package com.github.justadeni.standapi

import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot
import com.github.justadeni.standapi.Misc.sendTo
import com.github.justadeni.standapi.datatype.Offset
import com.github.justadeni.standapi.storage.StandApiConfig
import com.github.justadeni.standapi.datatype.Rotation
import com.github.justadeni.standapi.serialization.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * Packet representation of armorstand with all it's functions
 * @author justADeni
 */
@Serializable
class PacketStand(@Serializable(with = LocationSerializer::class) private var location: Location) {

    /**
     * entityId of the stand
     */
    val id = Misc.getID()

    /**
     * uniqueId of the stand
     */
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID()

    @Transient
    private val packetGen = PacketGenerator(id, uuid)

    private var attachedTo: Pair<@Serializable(with = UUIDSerializer::class) UUID, @Serializable(with = OffsetSerializer::class) Offset>? = null

    private var excludedPlayers = mutableListOf<@Serializable(with = UUIDSerializer::class) UUID>()

    @Serializable(with = PairSerializer::class)
    private val equipment = hashMapOf<ItemSlot,@Serializable(with = ItemStackSerializer::class) ItemStack>()

    private var isInvisible = 0x00 //0, 0x20
    private var hasGlowingEffect = 0x00 //0, 0x40

    private var customName = "" //2, String
    private var isCustomNameVisible = false //3, false

    private var isSmall = 0x00 //15, 0x01
    private var hasArms = 0x00 //15, 0x04
    private var hasNoBaseplate = 0x00 //15, 0x08
    private var isMarker = 0x00 //15, 0x10

    @Serializable(with = ListRotationSerializer::class)
    private val rotations = mutableListOf<@Serializable(with = RotationSerializer::class) Rotation>(
        Rotation(0f, 0f, 0f), //head 16
        Rotation(0f, 0f, 0f), //body 17
        Rotation(-10f, 0f, -10f), //left arm 18
        Rotation(-15f, 0f, 10f), //right arm 19
        Rotation(-1f, 0f, -1f), //left leg 20
        Rotation(1f, 0f, 1f) //right leg 21
    )

    @Transient
    internal val packetBundle = hashMapOf(Pair(0,packetGen.create(location)))
    @Transient
    internal val destroyPacket = packetGen.destroy()

    init {

        updateMetadata()

        if (equipment.isNotEmpty())
            packetBundle[1] = packetGen.equipment(equipment)

        val eligiblePlayers = eligiblePlayers()
        packetBundle.sendTo(eligiblePlayers)

        Ranger.add(this)
    }

    internal fun eligiblePlayers(): List<Player> = location.world!!.players.asSequence()
        .filter { it.location.distanceSquared(location) <= StandApiConfig.renderDistance2 }
        .filterNot { excludedPlayers.contains(it.uniqueId) }
        .toList()

    /**
     * used to make stand invisible to any number of chosen players
     * @param player which will be excluded from seeing and interacting with the stand
     */
    fun excludePlayer(player: Player){
        if (!excludedPlayers.contains(player.uniqueId))
            excludedPlayers.add(player.uniqueId)

        destroyPacket.sendTo(listOf(player))
    }

    /**
     * used to make stand visible again to any player that was previously excluded
     * @param player which will now be able to see and interact with the stand
     */
    fun unexcludePlayer(player: Player){
        excludedPlayers.remove(player.uniqueId)
        //Send All Packets
        if (eligiblePlayers().contains(player))
            packetBundle.sendTo(listOf(player))
    }

    /**
     * online players which are excluded from seeing the stand
     */
    fun excludedPlayers(): List<Player> = excludedPlayers.asSequence()
            .map { Bukkit.getPlayer(it) }
            .filterNotNull()
            .toList()

    /**
     * uuid's of players which are excluded from seeing the stand
     */
    fun excludedUUIDs(): List<UUID> = excludedPlayers.toList()

    /**
     * used to attach stand to entity
     * @param entity entity to which this stand will be attached to and follow
     */
    fun attachTo(entity: Entity){
        attachTo(entity, Offset.ZERO)
    }

    /**
     * used to attach stand to entity
     * @see Offset pass instance of this class with desired values
     * Offset within +- 4 blocks in any direction will ensure smoother movement
     *
     * @param entity entity to which this stand will be attached to and follow
     * @param offset relative offset of location from the entity
     */
    fun attachTo(entity: Entity, offset: Offset){
        Ranger.remove(this)
        setLocation(entity.location)
        attachedTo = Pair(entity.uniqueId, offset)
        Ranger.add(this)
    }

    /**
     * used to detach stand from any entity it was attached to
     */
    fun detachFrom(){
        Ranger.remove(this)
        attachedTo = null
        Ranger.add(this)
    }

    /**
     * pair of entity UUID to which the stand is attached to and offset. null if not attached
     */
    fun getAttached(): Pair<UUID, Offset>? = attachedTo

    /**
     * set equipment
     * @param slot to which the item will be equipped
     * @param item itemstack which will be assigned to the slot
     */
    fun setEquipment(slot: ItemSlot, item: ItemStack){
        equipment[slot] = item
        packetBundle[1] = packetGen.equipment(equipment).also { it.sendTo(eligiblePlayers()) }
    }

    private fun updateMetadata(){
        packetBundle[2] = packetGen.metadata(
            Pair((isInvisible or hasGlowingEffect).toByte(), (isSmall or hasArms or hasNoBaseplate or isMarker).toByte()),
            isCustomNameVisible,
            customName,
            rotations).also { it.sendTo(eligiblePlayers()) }
    }

    /**
     * get equipment
     * @param slot part of equipped stand
     * @return itemstack in that slot. null if none
     */
    fun getEquipment(slot: ItemSlot): ItemStack? {
        return equipment[slot]
    }

    /**
     * set stand visibility
     */
    fun setInvisible(value: Boolean){
        isInvisible = if (value) 0x20 else 0x00
        updateMetadata()
    }

    /**
     * get stand visibility
     * @return stand visibility
     */
    fun isInvisible(): Boolean = isInvisible > 0

    /**
     * set stand glowing
     * @param value sets stand glowing
     */
    fun setGlowingEffect(value: Boolean){
        hasGlowingEffect = if (value) 0x40 else 0x00
        updateMetadata()
    }

    /**
     * get stand glowing
     * @return stand glowing
     */
    fun hasGlowingEffect(): Boolean = hasGlowingEffect > 0

    /**
     * set custom name
     * @param value sets custom display name of stand
     */
    fun setCustomName(value: String){
        customName = value
        updateMetadata()
    }

    /**
     * get custom name
     * @return custom display name of stand
     */
    fun getCustomName(): String = customName

    /**
     * set custom name visibility
     * @param value sets custom name visibility
     */
    fun setCustomNameVisible(value: Boolean){
        isCustomNameVisible = value
        updateMetadata()
    }

    /**
     * get if custom name if visible
     * @return custom name visibility
     */
    fun isCustomNameVisible(): Boolean = isCustomNameVisible

    /**
     * set small
     * @param value sets small property
     */
    fun setSmall(value: Boolean){
        isSmall = if (value) 0x01 else 0x00
        updateMetadata()
    }

    /**
     * get small
     * @return small property
     */
    fun isSmall(): Boolean = isSmall > 0

    /**
     * set arms
     * @param value sets stand arms
     */
    fun setArms(value: Boolean){
        hasArms = if (value) 0x04 else 0x00
        updateMetadata()
    }

    /**
     * get if stand has arms
     * @return stand arms
     */
    fun hasArms(): Boolean = hasArms > 0

    /**
     * set no baseplate
     * @param value sets no baseplate
     */
    fun setNoBaseplate(value: Boolean){
        hasNoBaseplate = if (value) 0x08 else 0x00
        updateMetadata()
    }

    /**
     * get no baseplate
     * @return no baseplate
     */
    fun hasNoBaseplate(): Boolean = hasNoBaseplate > 0

    /**
     * set stand to be marker (non-existent hitbox)
     * @param value sets stand to be marker
     */
    fun setMarker(value: Boolean){
        isMarker = if (value) 0x10 else 0x00
        updateMetadata()
    }

    /**
     * get if stand is marker
     * @return if stand is marker
     */
    fun isMarker(): Boolean = isMarker > 0

    /**
     * move or teleport stand
     * @param loc where stand will move
     */
    fun setLocation(loc: Location){
        packetBundle[0] = packetGen.create(loc)

        if (loc.world != location.world){
            destroyPacket.sendTo(eligiblePlayers())
            location = loc
            packetBundle.sendTo(eligiblePlayers())

        } else if (loc.distanceSquared(location) > 64){

            for (player in eligiblePlayers())
                if (player.location.distanceSquared(location) > StandApiConfig.renderDistance2)
                    packetBundle.sendTo(listOf(player))
                else
                    packetGen.teleport(loc).sendTo(eligiblePlayers())

        } else {
            packetGen.move(location, loc).sendTo(eligiblePlayers())
        }

        location = loc
    }

    /**
     * get location of stand
     * @return location of stand
     */
    fun getLocation(): Location = location

    /**
     * set head pose
     * @param rotation pass instance of this class with desired values in degrees, 0-360
     */
    fun setHeadPose(rotation: Rotation){
        rotations[0] = rotation
        updateMetadata()
    }

    /**
     * get head pose
     * @return rotation of head
     */
    fun getHeadPose() = rotations[0]

    /**
     * set body pose
     * @param rotation pass instance of this class with desired values in degrees, 0-360
     */
    fun setBodyPose(rotation: Rotation){
        rotations[1] = rotation
        updateMetadata()
    }

    /**
     * get body pose
     * @return rotation of body
     */
    fun getBodyPose() = rotations[1]

    /**
     * set left arm pose
     * @param rotation pass instance of this class with desired values in degrees, 0-360
     */
    fun setLeftArmPose(rotation: Rotation){
        rotations[2] = rotation
        updateMetadata()
    }

    /**
     * get left arm pose
     * @return rotation of left arm
     */
    fun getLeftArmPose() = rotations[2]

    /**
     * set right arm pose
     * @param rotation pass instance of this class with desired values in degrees, 0-360
     */
    fun setRightArmPose(rotation: Rotation){
        rotations[3] = rotation
        updateMetadata()
    }

    /**
     * get right arm pose
     * @return rotation of right arm
     */
    fun getRightArmPose() = rotations[3]

    /**
     * set left leg pose
     * @param rotation pass instance of this class with desired values in degrees, 0-360
     */
    fun setLeftLegPose(rotation: Rotation){
        rotations[4] = rotation
        updateMetadata()
    }

    /**
     * get left leg pose
     * @return rotation of left leg
     */
    fun getLeftLegPose() = rotations[4]

    /**
     * set right leg pose
     * @param rotation pass instance of this class with desired values in degrees, 0-360
     */
    fun setRightlegPose(rotation: Rotation){
        rotations[5] = rotation
        updateMetadata()
    }

    /**
     * get right leg pose
     * @return rotation of right leg
     */
    fun getRightLegPose() = rotations[5]

    /**
     * removes the stand. dereference this instance to get it picked up by GC
     */
    fun remove(){
        Ranger.remove(this)
        destroyPacket.sendTo(location.world!!.players)
    }
}
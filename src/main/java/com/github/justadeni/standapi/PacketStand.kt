package com.github.justadeni.standapi

import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot
import com.github.justadeni.standapi.misc.Util.applyOffset
import com.github.justadeni.standapi.misc.Util.sendTo
import com.github.justadeni.standapi.misc.Util.squared
import com.github.justadeni.standapi.datatype.Offset
import com.github.justadeni.standapi.datatype.Rotation
import com.github.justadeni.standapi.datatype.Rotation.Companion.toEulerAngle
import com.github.justadeni.standapi.datatype.Rotation.Companion.toRotation
import com.github.justadeni.standapi.misc.Counter
import com.github.justadeni.standapi.misc.PacketGenerator
import com.github.justadeni.standapi.misc.Task
import com.github.justadeni.standapi.misc.Util
import com.github.justadeni.standapi.serialization.*
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.util.*

/**
 * Packet representation of armorstand with all it's functions
 * @author justADeni
 */
@Serializable
class PacketStand(
    @Serializable(with = LocationSerializer::class)
    private var location: Location) {

    constructor(location: Location, pluginName: String) : this(location) {
        this.pluginName = pluginName
    }

    /**
     * entityId of the stand
     */
    val id = Util.getID()

    /**
     * uniqueId of the stand
     */
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID()

    @Transient
    internal var pluginName: String = "None"

    @Transient
    internal val packetGen = PacketGenerator(id)

    @Transient
    private val counter = Counter(0, 10)

    private var attachedTo: Pair<@Serializable(with = UUIDSerializer::class) UUID, @Serializable(with = OffsetSerializer::class) Offset>? = null
    private var attachedPitch = true
    private var attachedYaw = true

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
    internal val rotations = mutableListOf<@Serializable(with = RotationSerializer::class) Rotation>(
        Rotation(0f, 0f, 0f), //head 16
        Rotation(0f, 0f, 0f), //body 17
        Rotation(0f, 0f, 0f), //left arm 18
        Rotation(0f, 0f, 0f), //right arm 19
        Rotation(0f, 0f, 0f), //left leg 20
        Rotation(0f, 0f, 0f) //right leg 21
    )

    @Transient
    internal val packetBundle = hashMapOf(Pair(0,packetGen.create(location, uuid)))
    @Transient
    internal val destroyPacket = packetGen.destroy()

    init {

        updateMetadata()

        if (equipment.isNotEmpty())
            packetBundle[1] = packetGen.equipment(equipment)

        val eligiblePlayers = eligiblePlayers()
        packetBundle.sendTo(eligiblePlayers)

        StandAPI.plugin().launch {
            StandManager.add(this@PacketStand)
        }
    }

    internal fun eligiblePlayers(): List<Player> = location.world?.players?.asSequence()
            ?.filter { it.location.distanceSquared(location) <= 192.squared() }
            ?.filterNot { excludedPlayers.contains(it.uniqueId) }
            ?.toList() ?: emptyList()

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
     * used to make stand invisible to any number of chosen players
     * use this method only for offline players
     * @param player which will be excluded from seeing and interacting with the stand
     */
    fun excludePlayer(uuid: UUID): PacketStand {
        if (!excludedPlayers.contains(uuid))
            excludedPlayers.add(uuid)

        return this
    }

    /**
     * used to make stand visible again to a player that was previously excluded
     * @param player which will now be able to see and interact with the stand
     */
    fun unexcludePlayer(player: Player): PacketStand {
        excludedPlayers.remove(player.uniqueId)
        //Send All Packets
        if (eligiblePlayers().contains(player))
            packetBundle.sendTo(listOf(player))

        return this
    }

    /**
     * used to make stand visible again to a player that was previously excluded
     * use this method only for offline players
     * @param uuid uuid of player which will now be able to see and interact with the stand
     */
    fun unexcludePlayer(uuid: UUID): PacketStand {
        excludedPlayers.remove(uuid)

        return this
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
     * @return returns back this instance
     */
    fun attachTo(entity: Entity): PacketStand {
        attachTo(entity, Offset.ZERO)

        return this
    }

    /**
     * used to attach stand to entity
     * @see Offset pass instance of this class with desired values
     * Offset within +- 4 blocks in any direction will ensure smoother movement
     *
     * @param entity entity to which this stand will be attached to and follow
     * @param offset relative offset of location from the entity
     * @return returns back this instance
     */
    fun attachTo(entity: Entity, offset: Offset): PacketStand {
        StandAPI.plugin().launch {
            StandManager.remove(this@PacketStand)
        }
        setLocation(entity.location.applyOffset(offset))
        attachedTo = Pair(entity.uniqueId, offset)
        StandAPI.plugin().launch {
            StandManager.add(this@PacketStand)
        }

        return this
    }

    /**
     * used to detach stand from any entity it was attached to
     * @return returns back this instance
     */
    fun detachFrom(): PacketStand {
        StandAPI.plugin().launch {
            StandManager.remove(this@PacketStand)
        }
        attachedTo = null
        StandAPI.plugin().launch {
            StandManager.add(this@PacketStand)
        }

        return this
    }

    /**
     * pair of entity UUID to which the stand is attached to and offset. null if not attached
     */
    fun getAttached(): Pair<UUID, Offset>? = attachedTo

    /**
     * should attached stand copy entity's pitch
     * @return returns back this instance
     */
    fun setAttachPitch(): PacketStand {
        attachedPitch = true

        return this
    }

    /**
     * is stand's pitch attached to entity
     */
    fun isAttachedPitch() = attachedPitch

    /**
     * should attached stand copy entity's yaw
     * @return returns back this instance
     */
    fun setAttachYaw(): PacketStand{
        attachedYaw = true

        return this
    }

    /**
     * is stand's yaw attached to entity
     */
    fun isAttachedYaw() = attachedYaw

    /**
     * set equipment
     * @param slot to which the item will be equipped
     * @param item itemstack which will be assigned to the slot
     * @return returns back this instance
     */
    fun setEquipment(slot: EquipmentSlot, item: ItemStack): PacketStand {
        equipment[Util.EQtoEW(slot)] = item
        packetBundle[1] = packetGen.equipment(equipment).also { it.sendTo(eligiblePlayers()) }

        return this
    }

    /**
     * get equipment
     * @param slot part of equipped stand
     * @return itemstack in that slot. null if none
     */
    fun getEquipment(slot: EquipmentSlot): ItemStack? {
        return equipment[Util.EQtoEW(slot)]
    }

    private fun updateMetadata(){
        packetBundle[2] = packetGen.metadata(
            Pair((isInvisible or hasGlowingEffect).toByte(), (isSmall or hasArms or hasNoBaseplate or isMarker).toByte()),
            isCustomNameVisible,
            customName,
            rotations).also { it.sendTo(eligiblePlayers()) }
    }

    /**
     * set stand visibility
     * @return returns back this instance
     */
    fun setVisible(value: Boolean): PacketStand {
        isInvisible = if (value) 0x00 else 0x20
        updateMetadata()

        return this
    }

    /**
     * get stand visibility
     * @return stand visibility
     */
    fun isVisible(): Boolean = isInvisible == 0

    /**
     * set stand glowing
     * @param value sets stand glowing
     * @return returns back this instance
     */
    fun setGlowingEffect(value: Boolean): PacketStand {
        hasGlowingEffect = if (value) 0x40 else 0x00
        updateMetadata()

        return this
    }

    /**
     * get stand glowing
     * @return stand glowing
     */
    fun hasGlowingEffect(): Boolean = hasGlowingEffect > 0

    /**
     * set custom name
     * @param value sets custom display name of stand
     * @return returns back this instance
     */
    fun setCustomName(value: String?): PacketStand {
        customName = value ?: ""
        updateMetadata()

        return this
    }

    /**
     * get custom name
     * @return custom display name of stand
     * @return returns back this instance
     */
    fun getCustomName(): String = customName

    /**
     * set custom name visibility
     * @param value sets custom name visibility
     * @return returns back this instance
     */
    fun setCustomNameVisible(value: Boolean): PacketStand {
        isCustomNameVisible = value
        updateMetadata()

        return this
    }

    /**
     * get if custom name if visible
     * @return custom name visibility
     */
    fun isCustomNameVisible(): Boolean = isCustomNameVisible

    /**
     * set small
     * @param value sets small property
     * @return returns back this instance
     */
    fun setSmall(value: Boolean): PacketStand {
        isSmall = if (value) 0x01 else 0x00
        updateMetadata()

        return this
    }

    /**
     * get small
     * @return small property
     */
    fun isSmall(): Boolean = isSmall > 0

    /**
     * set arms
     * @param value sets stand arms
     * @return returns back this instance
     */
    fun setArms(value: Boolean): PacketStand {
        hasArms = if (value) 0x04 else 0x00
        updateMetadata()

        return this
    }

    /**
     * get if stand has arms
     * @return stand arms
     */
    fun hasArms(): Boolean = hasArms > 0

    /**
     * set baseplate
     * @param value sets baseplate
     * @return returns back this instance
     */
    fun setBaseplate(value: Boolean): PacketStand {
        hasNoBaseplate = if (value) 0x00 else 0x08
        updateMetadata()

        return this
    }

    /**
     * get baseplate
     * @return baseplate
     */
    fun hasBaseplate(): Boolean = hasNoBaseplate == 0

    /**
     * set stand to be marker (non-existent hitbox)
     * @param value sets stand to be marker
     * @return returns back this instance
     */
    fun setMarker(value: Boolean): PacketStand {
        isMarker = if (value) 0x10 else 0x00
        updateMetadata()

        return this
    }

    /**
     * get if stand is marker
     * @return if stand is marker
     */
    fun isMarker(): Boolean = isMarker > 0

    /**
     * move or teleport stand
     * @param loc where stand will move
     * @return returns back this instance
     */
    fun setLocation(loc: Location): PacketStand {
        packetBundle[0] = packetGen.create(loc, uuid)

        if (loc.world != location.world){
            destroyPacket.sendTo(eligiblePlayers())
            location = loc
            packetBundle.sendTo(eligiblePlayers())

        } else if (loc.distanceSquared(location) > 64){

            for (player in eligiblePlayers())
                if (player.location.distanceSquared(location) > 192.squared() )
                    packetBundle.sendTo(listOf(player))
                else
                    packetGen.teleport(loc).sendTo(eligiblePlayers())

        } else {
            //Uses teleport once in a while to guarantee precise location without sending too many big packets too often
            if (counter.reached())
                packetGen.teleport(loc).sendTo(eligiblePlayers())
            else
                packetGen.move(location, loc).sendTo(eligiblePlayers())
        }

        location = loc

        return this
    }

    internal fun setLocationNoUpdate(loc: Location): PacketStand {
        location = loc
        packetBundle[0] = packetGen.create(loc, uuid)

        return this
    }

    /**
     * get location of stand
     * @return location of stand
     */
    fun getLocation(): Location = location

    /**
     * set head pose
     * @param rotation pass instance of this class with desired values in degrees, 0-360
     * @return returns back this instance
     */
    fun setHeadPose(rotation: Rotation): PacketStand {
        rotations[0] = rotation
        updateMetadata()

        return this
    }

    /**
     * get head pose
     * @return rotation of head
     */
    fun getHeadPose() = rotations[0]

    /**
     * set body pose
     * @param rotation pass instance of this class with desired values in degrees, 0-360
     * @return returns back this instance
     */
    fun setBodyPose(rotation: Rotation): PacketStand {
        rotations[1] = rotation
        updateMetadata()

        return this
    }

    /**
     * get body pose
     * @return rotation of body
     */
    fun getBodyPose() = rotations[1]

    /**
     * set left arm pose
     * @param rotation pass instance of this class with desired values in degrees, 0-360
     * @return returns back this instance
     */
    fun setLeftArmPose(rotation: Rotation): PacketStand {
        rotations[2] = rotation
        updateMetadata()

        return this
    }

    /**
     * get left arm pose
     * @return rotation of left arm
     */
    fun getLeftArmPose() = rotations[2]

    /**
     * set right arm pose
     * @param rotation pass instance of this class with desired values in degrees, 0-360
     * @return returns back this instance
     */
    fun setRightArmPose(rotation: Rotation): PacketStand {
        rotations[3] = rotation
        updateMetadata()

        return this
    }

    /**
     * get right arm pose
     * @return rotation of right arm
     */
    fun getRightArmPose() = rotations[3]

    /**
     * set left leg pose
     * @param rotation pass instance of this class with desired values in degrees, 0-360
     * @return returns back this instance
     */
    fun setLeftLegPose(rotation: Rotation): PacketStand {
        rotations[4] = rotation
        updateMetadata()

        return this
    }

    /**
     * get left leg pose
     * @return rotation of left leg
     */
    fun getLeftLegPose() = rotations[4]

    /**
     * set right leg pose
     * @param rotation pass instance of this class with desired values in degrees, 0-360
     * @return returns back this instance
     */
    fun setRightlegPose(rotation: Rotation): PacketStand {
        rotations[5] = rotation
        updateMetadata()

        return this
    }

    /**
     * get right leg pose
     * @return rotation of right leg
     */
    fun getRightLegPose() = rotations[5]

    /**
     * removes the stand
     * remember to remove it from your lists/maps/sets if it was there
     */
    fun remove(){
        StandAPI.plugin().launch(StandAPI.plugin().asyncDispatcher) {
            StandManager.remove(this@PacketStand)
        }
        destroyPacket.sendTo(location.world!!.players)
    }

    /**
     * removes the stand after specified number of ticks
     * remember to remove it from your lists/maps/sets if it was there
     */
    fun remove(ticks: Int){
        StandAPI.plugin().launch(StandAPI.plugin().asyncDispatcher) {
            delay(ticks.ticks)
            remove()
        }
    }

    /**
     * removes the stand after specified number of ticks and executes specified task (use lambda syntax)
     * remember to remove it from your lists/maps/sets if it was there
     */
    fun removeAndExecute(ticks: Int, task: Task){
        StandAPI.plugin().launch(StandAPI.plugin().asyncDispatcher) {
            delay(ticks.ticks)
            remove()
            task.execute()
        }
    }

    /**
     * Converts PacketStand to a real entity with all it's properties
     * and removes the former
     * @return returns the real entity
     */
    fun toRealStand(): ArmorStand {
        val realStand = runBlocking(StandAPI.plugin().minecraftDispatcher) {
            location.world?.spawnEntity(location, EntityType.ARMOR_STAND) as ArmorStand
        }
        realStand.setArms(hasArms())
        realStand.setBasePlate(hasBaseplate())
        realStand.isMarker = isMarker()
        realStand.isSmall = isSmall()
        realStand.isVisible = isVisible()
        realStand.isCustomNameVisible = isCustomNameVisible
        realStand.customName = customName

        for (itemSlotItemPair in equipment) {
            realStand.equipment?.setItem(Util.EWtoEQ(itemSlotItemPair.key), itemSlotItemPair.value)
        }

        realStand.headPose = getHeadPose().toEulerAngle()
        realStand.bodyPose = getBodyPose().toEulerAngle()
        realStand.leftArmPose = getLeftArmPose().toEulerAngle()
        realStand.rightArmPose = getRightArmPose().toEulerAngle()
        realStand.leftLegPose = getLeftLegPose().toEulerAngle()
        realStand.rightLegPose = getRightLegPose().toEulerAngle()

        remove()

        return realStand
    }

    companion object {

        /**
         * Converts Bukkit Armorstand to PacketStand with all it's properties
         * and removes the former
         * @return returns the fake entity
         */
        @JvmStatic
        fun ArmorStand.fromRealStand(): PacketStand {
            val packetStand = PacketStand(this.location)
            packetStand.setArms(this.hasArms())
                .setBaseplate(this.hasBasePlate())
                .setMarker(this.isMarker)
                .setSmall(this.isSmall)
                .setVisible(this.isVisible)
                .setCustomNameVisible(this.isCustomNameVisible)
                .setCustomName(this.customName)

            for (equipmentSlot in EquipmentSlot.entries){
                val equipment = this.equipment?.getItem(equipmentSlot) ?: continue
                packetStand.setEquipment(equipmentSlot, equipment)
            }

            packetStand.setHeadPose(this.headPose.toRotation())
            packetStand.setBodyPose(this.bodyPose.toRotation())
            packetStand.setLeftArmPose(this.leftArmPose.toRotation())
            packetStand.setRightArmPose(this.rightArmPose.toRotation())
            packetStand.setLeftLegPose(this.leftLegPose.toRotation())
            packetStand.setRightlegPose(this.rightLegPose.toRotation())

            StandAPI.plugin().launch(StandAPI.plugin().minecraftDispatcher) {
                this@fromRealStand.remove()
            }

            return packetStand
        }
    }
}
package com.github.justadeni.standapi

import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot
import com.github.justadeni.standapi.Misc.sendTo
import com.github.justadeni.standapi.datatype.Offset
import com.github.justadeni.standapi.storage.Config
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

@Serializable
class PacketStand(@Serializable(with = LocationSerializer::class) private var location: Location) {

    val id = Misc.getID()

    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID = UUID.randomUUID()

    @Transient
    private val packetGen = PacketGenerator(id, uuid)

    @Transient
    internal val includedPlayers = mutableListOf<Player?>()

    private var attachedTo: Pair<@Serializable(with = UUIDSerializer::class) UUID, @Serializable(with = OffsetSerializer::class) Offset>? = null

    private var excludedPlayers = mutableListOf<@Serializable(with = UUIDSerializer::class) UUID>()

    /*@Serializable(with = PairSerializer::class)
    private val equipment = mutableListOf<Pair<ItemSlot, ItemStack>>()*/
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
        Ranger.add(this)

        updateMetadata()

        if (equipment.isNotEmpty())
            packetBundle[1] = packetGen.equipment(equipment)

        val eligiblePlayers = eligiblePlayers()
        packetBundle.sendTo(eligiblePlayers)
        includedPlayers.addAll(eligiblePlayers)

        if (attachedTo != null){
            val pE = Bukkit.getEntity(attachedTo!!.first)
            if (pE != null){
                setLocation(Location(pE.world, pE.location.x + attachedTo!!.second.x, pE.location.y + attachedTo!!.second.y, pE.location.z + attachedTo!!.second.z))
            } else {
                attachedTo = null
            }
        }
    }

    internal fun eligiblePlayers(): List<Player> = location.world!!.players.asSequence()
        .filter { it.location.distanceSquared(location) <= Config.renderDistance2 }
        .filterNot { excludedPlayers.contains(it.uniqueId) }
        .toList()

    fun excludePlayer(player: Player){
        if (!excludedPlayers.contains(player.uniqueId))
            excludedPlayers.add(player.uniqueId)

        destroyPacket.sendTo(listOf(player))
    }

    fun unexcludePlayer(player: Player){
        excludedPlayers.remove(player.uniqueId)
        //Send All Packets
        if (eligiblePlayers().contains(player))
            packetBundle.sendTo(listOf(player))
    }

    fun excludedPlayers(): List<Player> = excludedPlayers.asSequence()
            .map { Bukkit.getPlayer(it) }
            .filterNotNull()
            .toList()

    fun attachTo(entity: Entity){
        attachTo(entity, Offset.ZERO)
    }

    fun attachTo(entity: Entity, offset: Offset){
        setLocation(entity.location)
        attachedTo = Pair(entity.uniqueId, offset)
    }

    fun detachFrom(){
        attachedTo = null
    }

    fun getAttached(): Pair<UUID, Offset>? = attachedTo

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

    fun getEquipment(slot: ItemSlot): ItemStack? {
        return equipment[slot]
    }

    fun setInvisible(value: Boolean){
        isInvisible = if (value) 0x20 else 0x00
        updateMetadata()
    }

    fun isInvisible(): Boolean = isInvisible > 0

    fun setGlowingEffect(value: Boolean){
        hasGlowingEffect = if (value) 0x40 else 0x00
        updateMetadata()
    }

    fun hasGlowingEffect(): Boolean = hasGlowingEffect > 0

    fun setCustomName(value: String){
        customName = value
        updateMetadata()
    }

    fun getCustomName(): String = customName

    fun setCustomNameVisible(value: Boolean){
        isCustomNameVisible = value
        updateMetadata()
    }

    fun isCustomNameVisible(): Boolean = isCustomNameVisible

    fun setSmall(value: Boolean){
        isSmall = if (value) 0x01 else 0x00
        updateMetadata()
    }

    fun isSmall(): Boolean = isSmall > 0

    fun setArms(value: Boolean){
        hasArms = if (value) 0x04 else 0x00
        updateMetadata()
    }

    fun hasArms(): Boolean = hasArms > 0

    fun setNoBaseplate(value: Boolean){
        hasNoBaseplate = if (value) 0x08 else 0x00
        updateMetadata()
    }

    fun hasNoBaseplate(): Boolean = hasNoBaseplate > 0

    fun setMarker(value: Boolean){
        isMarker = if (value) 0x10 else 0x00
        updateMetadata()
    }

    fun isMarker(): Boolean = isMarker > 0

    fun setLocation(loc: Location){
        packetBundle[0] = packetGen.create(loc)

        if (loc.world != location.world){
            Ranger.remove(this)
            destroyPacket.sendTo(eligiblePlayers())
            location = loc
            Ranger.add(this)
            packetBundle.sendTo(eligiblePlayers())

        } else if (loc.distanceSquared(location) > 64){

            for (player in eligiblePlayers())
                if (player.location.distanceSquared(location) > Config.renderDistance2)
                    packetBundle.sendTo(listOf(player))
                else
                    packetGen.teleport(loc).sendTo(eligiblePlayers())

        } else {
            packetGen.move(location, loc).sendTo(eligiblePlayers())
        }

        location = loc
    }

    fun getLocation(): Location = location

    fun setHeadPose(rotation: Rotation){
        rotations[0] = rotation
        updateMetadata()
    }

    fun getHeadPose() = rotations[0]

    fun setBodyPose(rotation: Rotation){
        rotations[1] = rotation
        updateMetadata()
    }

    fun getBodyPose() = rotations[1]

    fun setLeftArmPose(rotation: Rotation){
        rotations[2] = rotation
        updateMetadata()
    }

    fun getLeftArmPose() = rotations[2]

    fun setRightArmPose(rotation: Rotation){
        rotations[3] = rotation
        updateMetadata()
    }

    fun getRightArmPose() = rotations[3]

    fun setLeftLegPose(rotation: Rotation){
        rotations[4] = rotation
        updateMetadata()
    }

    fun getLeftLegPose() = rotations[4]

    fun setRightlegPose(rotation: Rotation){
        rotations[5] = rotation
        updateMetadata()
    }

    fun getRightLegPose() = rotations[5]

    fun remove(){
        Ranger.remove(this)
        destroyPacket.sendTo(location.world!!.players)
        /*
        StandAPI.getPlugin().launch {
            withContext(StandAPI.getPlugin().asyncDispatcher) {
                delay(120.ticks)
                destroyPacket.sendTo(location.world!!.players)
            }
        }
        */
    }
}
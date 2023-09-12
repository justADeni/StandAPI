package com.github.justadeni.standapi

import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot
import com.comphenix.protocol.wrappers.Pair
import com.github.justadeni.standapi.Misc.sendTo
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.minecraft.core.Rotations
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.math.sqrt

class PacketStand(location: Location) {

    val id = Misc.getID()
    val uuid = UUID.randomUUID()
    private val packetGen = PacketGenerator(id, uuid)

    private var location = location
    private var renderDistance2 = 9216 //6 chunks
    internal val includedPlayers = mutableListOf<Player?>()
    private var excludedPlayers = mutableListOf<Player?>()

    private val equipment = mutableListOf<Pair<ItemSlot, ItemStack>>()

    private var isInvisible = 0x00 //0, 0x20
    private var hasGlowingEffect = 0x00 //0, 0x40

    private var customName = "" //2, String
    private var isCustomNameVisible = false //3, false

    private var isSmall = 0x00 //15, 0x01
    private var hasArms = 0x00 //15, 0x04
    private var hasNoBaseplate = 0x00 //15, 0x08
    private var isMarker = 0x00 //15, 0x10

    private val rotations = mutableListOf(
        Rotations(0f, 0f, 0f), //head 16
        Rotations(0f, 0f, 0f), //body 17
        Rotations(-10f, 0f, -10f), //left arm 18
        Rotations(-15f, 0f, 10f), //right arm 19
        Rotations(-1f, 0f, -1f), //left leg 20
        Rotations(1f, 0f, 1f) //right leg 21
    )

    internal var packetBundle = hashMapOf(kotlin.Pair(0,packetGen.create(location)))
    internal var destroyPacket = packetGen.destroy()

    init {
        Ranger.add(this)
        packetBundle.sendTo(eligiblePlayers())
        includedPlayers.addAll(eligiblePlayers())
    }

    internal fun eligiblePlayers(): List<Player> = location.world!!.players.asSequence()
        .filter { it.location.distanceSquared(location) <= renderDistance2 }
        .filterNot { excludedPlayers.contains(it) }
        .toList()

    fun excludePlayer(player: Player){
        if (!excludedPlayers.contains(player))
            excludedPlayers.add(player)

        destroyPacket.sendTo(listOf(player))
    }

    fun unexcludePlayer(player: Player){
        excludedPlayers.remove(player)
        //Send All Packets
        if (eligiblePlayers().contains(player))
            packetBundle.sendTo(listOf(player))
    }

    fun excludedPlayers(): List<Player?> {
        excludedPlayers = excludedPlayers.filterNotNull().toMutableList()
        return excludedPlayers
    }

    fun setRenderDistance(chunks: Int){
        renderDistance2 = (chunks * 16)*(chunks * 16)
    }

    fun getRenderDistance(): Int {
        return sqrt((renderDistance2 / 16).toDouble()).toInt()
    }

    fun setEquipment(slot: ItemSlot, item: ItemStack){
        val duplicateChecked = equipment.asSequence()
            .onEach {
                if (it.first == slot)
                    it.second = item
            }
            .toMutableList()

        if (duplicateChecked == equipment)
            duplicateChecked.add(Pair(slot, item))

        packetBundle[1] = packetGen.equipment(duplicateChecked).also { it.sendTo(eligiblePlayers()) }
    }

    private fun updateMetadata(){
        packetBundle[2] = packetGen.metadata(
            kotlin.Pair((isInvisible or hasGlowingEffect).toByte(), (isSmall or hasArms or hasNoBaseplate or isMarker).toByte()),
            isCustomNameVisible,
            customName,
            rotations).also { it.sendTo(eligiblePlayers()) }
    }

    fun getEquipment(slot: ItemSlot): ItemStack? {
        return equipment.firstOrNull { it.first == slot }?.second
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
                if (player.location.distanceSquared(location) > renderDistance2)
                    packetBundle.sendTo(listOf(player))
                else
                    packetGen.teleport(loc).sendTo(eligiblePlayers())

        } else {
            packetGen.move(location, loc).sendTo(eligiblePlayers())
        }

        location = loc
    }

    fun getLocation(): Location = location

    fun setHeadPose(rotation: Rotations){
        rotations[0] = rotation
        updateMetadata()
    }

    fun getHeadPose() = rotations[0]

    fun setBodyPose(rotation: Rotations){
        rotations[1] = rotation
        updateMetadata()
    }

    fun getBodyPose() = rotations[1]

    fun setLeftArmPose(rotation: Rotations){
        rotations[2] = rotation
        updateMetadata()
    }

    fun getLeftArmPose() = rotations[2]

    fun setRightArmPose(rotation: Rotations){
        rotations[3] = rotation
        updateMetadata()
    }

    fun getRightArmPose() = rotations[3]

    fun setLeftLegPose(rotation: Rotations){
        rotations[4] = rotation
        updateMetadata()
    }

    fun getLeftLegPose() = rotations[4]

    fun setRightlegPose(rotation: Rotations){
        rotations[5] = rotation
        updateMetadata()
    }

    fun getRightLegPose() = rotations[5]

    fun remove(){
        Ranger.remove(this)
        destroyPacket.sendTo(location.world!!.players)

        StandAPI.getPlugin().launch {
            withContext(StandAPI.getPlugin().asyncDispatcher) {
                delay(120.ticks)
                destroyPacket.sendTo(location.world!!.players)
            }
        }
    }
}
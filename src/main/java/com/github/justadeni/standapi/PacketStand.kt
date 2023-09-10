package com.github.justadeni.standapi

import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot
import com.comphenix.protocol.wrappers.Pair
import com.github.justadeni.standapi.Misc.sendTo
import net.minecraft.core.Rotations
import net.minecraft.server.packs.repository.Pack
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
    private val excludedPlayers = mutableSetOf<UUID>()
    private val equipment = mutableListOf<Pair<ItemSlot, ItemStack>>()

    private var isInvisible = false //0, 0x20
    private var hasGlowingEffect = false //0, 0x40

    private var customName = "" //2, String
    private var isCustomNameVisible = false //3, false

    private var isSmall = false //15, 0x01
    private var hasArms = false //15, 0x04
    private var hasNoBaseplate = false //15, 0x08

    private var rotations = listOf(
        Rotations(0f, 0f, 0f), //head 16
        Rotations(0f, 0f, 0f), //body 17
        Rotations(-10f, 0f, -10f), //left arm 18
        Rotations(-15f, 0f, 10f), //right arm 19
        Rotations(-1f, 0f, -1f), //left leg 20
        Rotations(1f, 0f, 1f) //right leg 21
    )

    private var packetBundle = hashMapOf(kotlin.Pair(0,packetGen.create(location)))
    private var destroyPacket = packetGen.destroy()





    private fun eligiblePlayers(): Set<Player> = location.world!!.players.asSequence()
        .filter { it.location.distanceSquared(location) <= renderDistance2 }
        .filterNot { excludedPlayers.contains(it.uniqueId) }
        .toSet()

    fun excludePlayer(player: Player){
        excludedPlayers.add(player.uniqueId)
        //Send Death Packet
    }

    fun unexcludePlayer(player: Player){
        excludedPlayers.remove(player.uniqueId)
        //Send All Packets
    }

    fun setRenderDistance(chunks: Int){
        renderDistance2 = (chunks*16)*(chunks*16)
    }

    fun getRenderDistance(): Int {
        return (sqrt(renderDistance2.toDouble()) / 16).toInt()
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

        packetBundle[1] = packetGen.equipment(duplicateChecked)
    }

    private fun updateMetadata(){

    }

    fun getEquipment(slot: ItemSlot): ItemStack? {
        return equipment.firstOrNull { it.first == slot }?.second
    }

    fun setInvisible(value: Boolean){
        if (value == isInvisible)
            return

        isInvisible = value
        updateMetadata()
    }

    fun isInvisible(): Boolean = isInvisible

    fun setGlowingEffect(value: Boolean){
        if (value == hasGlowingEffect)
            return

        hasGlowingEffect = value
        updateMetadata()
    }

    fun hasGlowingEffect(): Boolean = isInvisible

    fun setCustomName(value: String){
        if (value == customName)
            return

        customName = value
        updateMetadata()
    }

    fun getCustomName(): String = customName

    fun setCustomNameVisible(value: Boolean){
        if (value == isCustomNameVisible)
            return

        isInvisible = value
        updateMetadata()
    }

    fun isCustomNameVisible(): Boolean = isCustomNameVisible

    fun setSmall(value: Boolean){
        if (value == isSmall)
            return

        isSmall = value
        updateMetadata()
    }

    fun isSmall(): Boolean = isSmall

    fun setArms(value: Boolean){
        if (value == hasArms)
            return

        hasArms = value
        updateMetadata()
    }

    fun hasArms(): Boolean = hasArms

    fun setNoBaseplate(value: Boolean){
        if (value == hasNoBaseplate)
            return

        hasNoBaseplate = value
        updateMetadata()
    }

    fun hasNoBaseplate(): Boolean = hasNoBaseplate
}
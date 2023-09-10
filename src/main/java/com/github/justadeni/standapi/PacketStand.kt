package com.github.justadeni.standapi

import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot
import net.minecraft.core.Rotations
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket.Rot
import net.minecraft.world.entity.EquipmentSlot
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.EulerAngle
import java.util.*
import kotlin.math.sqrt

class PacketStand(location: Location) {

    val id = Misc.getID()
    val uuid = UUID.randomUUID()

    private var location = location
    private var renderDistance2 = 9216 //6 chunks
    private val excludedPlayers = mutableSetOf<UUID>()
    private val equipment = hashMapOf<ItemSlot, ItemStack>()

    private var isInvisible = false //0, 0x20
    private var hasGlowingEffect = false //0, 0x40

    private var customName = "" //2, ?
    private var isCustomNameVisible = false //3, false

    private var isSmall = false //15, 0x01
    private var hasArms = false //15, 0x04
    private var hasNoBaseplate = false //15, 0x08

    private var headRotation = Rotations(0f, 0f, 0f) //16
    private var bodyRotation = Rotations(0f, 0f, 0f) //17
    private var leftArmRotation = Rotations(-10f, 0f, -10f) //18
    private var rightArmRotation = Rotations(-15f, 0f, 10f) //19
    private var leftLegRotation = Rotations(-1f, 0f, -1f) //20
    private var rightLegRotation = Rotations(1f, 0f, 1f) //21

    private fun eligiblePlayerss(): Set<Player> = location.world!!.players.asSequence()
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
        equipment[slot] = item
        //recompile and send item packet
    }
}
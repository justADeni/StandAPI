package com.github.justadeni.standapi.stat

import com.comphenix.protocol.events.PacketContainer
import com.github.justadeni.standapi.Misc
import com.github.justadeni.standapi.Misc.sendTo
import com.github.justadeni.standapi.PacketGenerator
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.EulerAngle
import java.util.*
import kotlin.collections.HashSet


class StaticStand(location: Location, eulerAngle: EulerAngle, itemStack: ItemStack) {

    val id = Misc.getID()
    val uuid = UUID.randomUUID()
    private val players: HashSet<Player> = HashSet()

    private val packetGenerator = PacketGenerator(id, uuid)
    private val createPacket: PacketContainer = packetGenerator.create(location)
    private val destroyPacket: PacketContainer = packetGenerator.destroy()
    private val rotationPacket: PacketContainer = packetGenerator.headrotation(eulerAngle)
    private val equipmentPacket: PacketContainer = packetGenerator.equipment(itemStack)
    //var locationPacket: PacketContainer = null

    fun getSeeing(): Set<Player>{
        return players
    }
    fun addSeeing(deltaP: List<Player>){
        players.addAll(deltaP)
        createPacket.sendTo(deltaP)
        rotationPacket.sendTo(deltaP)
        equipmentPacket.sendTo(deltaP)
    }

    fun removeSeeing(deltaP: List<Player>){
        players.removeAll(deltaP.toSet())
        destroyPacket.sendTo(deltaP)
    }

}
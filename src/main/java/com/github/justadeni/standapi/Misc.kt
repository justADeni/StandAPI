package com.github.justadeni.standapi

import com.comphenix.protocol.events.PacketContainer
import org.bukkit.entity.Player

object Misc {

    private var id = 9999

    fun currentID() = id

    fun getID(): Int {
        id += 1
        return id
    }

    fun PacketContainer.sendTo(players: List<Player>){
        val manager = StandAPI.getManager()
        for (player in players){
            manager.sendServerPacket(player, this)
        }
    }

    fun HashMap<Int, PacketContainer>.sendTo(players: List<Player>){
        val manager = StandAPI.getManager()
        for (player in players){
            this.values.forEach { manager.sendServerPacket(player, it) }
        }
    }
}
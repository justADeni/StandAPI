package com.github.justadeni.standapi

import com.comphenix.protocol.events.PacketContainer
import org.bukkit.entity.Player

object Misc {

    private var id = 9999

    private val manager = StandAPI.getManager()

    internal fun currentID() = id

    internal fun getID(): Int {
        id += 1
        return id
    }

    internal fun PacketContainer.sendTo(player: Player){
        manager.sendServerPacket(player, this)
    }

    internal fun PacketContainer.sendTo(players: List<Player>){
        for (player in players){
            manager.sendServerPacket(player, this)
        }
    }

    internal fun HashMap<Int, PacketContainer>.sendTo(players: List<Player>){
        for (player in players){
            this.values.forEach { manager.sendServerPacket(player, it) }
        }
    }

    internal fun Int.squared(): Int{
        return this*this
    }
}
package com.github.justadeni.standapi

import com.comphenix.protocol.events.PacketContainer
import com.github.shynixn.mccoroutine.bukkit.launch
import org.bukkit.entity.Player

object Misc {

    private var id = 9999

    private val manager = StandAPI.getManager()

    internal fun currentID() = id

    internal fun getID(): Int {
        id += 1
        return id
    }

    internal fun PacketContainer.sendTo(player: Player) = StandAPI.getPlugin().launch {
        manager.sendServerPacket(player, this@sendTo)
    }

    internal fun PacketContainer.sendTo(players: List<Player>) = StandAPI.getPlugin().launch {
        for (player in players){
            manager.sendServerPacket(player, this@sendTo)
        }
    }

    internal fun HashMap<Int, PacketContainer>.sendTo(player: Player) = StandAPI.getPlugin().launch {
        this@sendTo.values.forEach { manager.sendServerPacket(player, it) }
    }

    internal fun HashMap<Int, PacketContainer>.sendTo(players: List<Player>) = StandAPI.getPlugin().launch {
        for (player in players){
            this@sendTo.values.forEach { manager.sendServerPacket(player, it) }
        }
    }

    internal fun Int.squared(): Int{
        return this*this
    }
}
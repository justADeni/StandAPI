package com.github.justadeni.standapi.event

import com.github.justadeni.standapi.PacketStand
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PacketStandEvent(val player: Player, val id: Int, val packetStand: PacketStand?, val action: Action) : Event() {
    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
        val handlerList = HandlerList()
    }
}

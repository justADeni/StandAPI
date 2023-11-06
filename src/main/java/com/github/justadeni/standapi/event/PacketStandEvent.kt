package com.github.justadeni.standapi.event

import com.github.justadeni.standapi.PacketStand
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Event triggered by right or left clicking a PacketStand
 * @param player player which clicked
 * @param id id of the armorstand
 * @param packetStand stand that was clicked
 * @param action which action player did
 */
class PacketStandEvent(val player: Player, val id: Int, val packetStand: PacketStand?, val action: Action) : Event(true) {
    override fun getHandlers(): HandlerList {
        return HANDLERS
    }

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }
}

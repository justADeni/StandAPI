package com.github.justadeni.standapi.attachment.server

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.github.justadeni.standapi.misc.Util.applyOffset
import com.github.justadeni.standapi.StandManager
import com.github.justadeni.standapi.StandAPI
import org.bukkit.Location

/**
 * @suppress
 */
class TeleportListener {
    init {
        StandAPI.manager().addPacketListener(object : PacketAdapter(StandAPI.plugin(), ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_TELEPORT) {
            override fun onPacketSending(event: PacketEvent) {
                val player = event.player
                val packet = event.packet
                val entityId = packet.integers.read(0)

                val list = StandManager.attachedTo(entityId) ?: return

                val loc = Location(player.world, packet.doubles.read(0), packet.doubles.read(1), packet.doubles.read(2))
                for (stand in list){
                    stand.setLocation(loc.applyOffset(stand.getAttached()?.second))
                }
            }
        })
    }
}
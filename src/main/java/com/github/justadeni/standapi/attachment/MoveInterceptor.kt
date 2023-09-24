package com.github.justadeni.standapi.attachment

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.github.justadeni.standapi.Misc.sendTo
import com.github.justadeni.standapi.Ranger
import com.github.justadeni.standapi.StandAPI
import com.github.shynixn.mccoroutine.bukkit.launch

class MoveInterceptor {
    init {
        StandAPI.getManager().addPacketListener(object : PacketAdapter(StandAPI.getPlugin(), ListenerPriority.NORMAL, PacketType.Play.Server.REL_ENTITY_MOVE) {
            override fun onPacketSending(event: PacketEvent) {
                val player = event.player
                val packet = event.packet
                val entityId = packet.integers.read(0)

                val list = Ranger.findByEntityId(entityId)
                if (list.isEmpty())
                    return

                for (stand in list)
                    packet.deepClone().also { it.integers.write(0, stand.id) }.sendTo(player)
            }
        })
    }
}
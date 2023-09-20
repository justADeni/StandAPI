package com.github.justadeni.standapi.attachment

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.github.justadeni.standapi.Misc.sendTo
import com.github.justadeni.standapi.StandAPI
import com.github.justadeni.standapi.datatype.Offset
import com.github.shynixn.mccoroutine.bukkit.launch

class RemovalInterceptor {
    init {
        StandAPI.getManager().addPacketListener(object : PacketAdapter(StandAPI.getPlugin(), ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_DESTROY) {
            override fun onPacketReceiving(event: PacketEvent) {
                //val player = event.player
                val packet = event.packet
                val id = packet.integers.read(0)

                if (Attacher.getMap().keys.contains(id))
                    Attacher.remove(id)
            }
        })
    }
}
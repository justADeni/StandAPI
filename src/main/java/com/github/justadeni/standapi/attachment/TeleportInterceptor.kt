package com.github.justadeni.standapi.attachment

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.github.justadeni.standapi.Misc.sendTo
import com.github.justadeni.standapi.StandAPI
import com.github.justadeni.standapi.datatype.Offset
import com.github.shynixn.mccoroutine.bukkit.launch

class TeleportInterceptor {
    init {
        StandAPI.getManager().addPacketListener(object : PacketAdapter(StandAPI.getPlugin(), ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_TELEPORT) {
            override fun onPacketReceiving(event: PacketEvent) {
                val player = event.player
                val packet = event.packet
                val id = packet.integers.read(0)

                val attachedMap = Attacher.getMap()
                if (!attachedMap.keys().contains(id))
                    return

                val list = attachedMap[id]
                for (pair in list){

                    if (pair.second == Offset.ZERO) {
                        StandAPI.getPlugin().launch { packet.deepClone().also { it.integers.write(0, pair.first) }.sendTo(listOf(player)) }
                        continue
                    }

                    val altPacket = packet.deepClone().also { it.integers.write(0, pair.first) }
                    altPacket.doubles.write(0,altPacket.doubles.read(0) + pair.second.x)
                    altPacket.doubles.write(1,altPacket.doubles.read(1) + pair.second.y)
                    altPacket.doubles.write(2,altPacket.doubles.read(2) + pair.second.z)
                    StandAPI.getPlugin().launch { altPacket.sendTo(player) }
                }
            }
        })
    }
}
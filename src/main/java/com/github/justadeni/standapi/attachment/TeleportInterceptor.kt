package com.github.justadeni.standapi.attachment

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.github.justadeni.standapi.Misc.sendTo
import com.github.justadeni.standapi.Ranger
import com.github.justadeni.standapi.StandAPI
import com.github.justadeni.standapi.datatype.Offset

/**
 * @suppress
 */
class TeleportInterceptor {
    init {
        StandAPI.manager().addPacketListener(object : PacketAdapter(StandAPI.plugin(), ListenerPriority.NORMAL, PacketType.Play.Server.ENTITY_TELEPORT) {
            override fun onPacketSending(event: PacketEvent) {
                val player = event.player
                val packet = event.packet
                val entityId = packet.integers.read(0)

                val list = Ranger.findByEntityId(entityId)
                for (stand in list){

                    if (stand.getAttached()!!.second == Offset.ZERO) {
                        packet.deepClone().also { it.integers.write(0, stand.id) }.sendTo(listOf(player))
                        continue
                    }

                    val altPacket = packet.deepClone().also { it.integers.write(0, stand.id) }
                    altPacket.doubles.write(0,altPacket.doubles.read(0) + stand.getAttached()!!.second.x)
                    altPacket.doubles.write(1,altPacket.doubles.read(1) + stand.getAttached()!!.second.y)
                    altPacket.doubles.write(2,altPacket.doubles.read(2) + stand.getAttached()!!.second.z)
                    altPacket.sendTo(player)
                }
            }
        })
    }
}
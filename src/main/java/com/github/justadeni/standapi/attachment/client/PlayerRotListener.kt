package com.github.justadeni.standapi.attachment.client

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.github.justadeni.standapi.Ranger
import com.github.justadeni.standapi.StandAPI
import org.bukkit.Location

/**
 * @suppress
 */
class PlayerRotListener {
    init {
        StandAPI.manager().addPacketListener(object : PacketAdapter(StandAPI.plugin(), ListenerPriority.LOWEST, PacketType.Play.Client.LOOK) {
            override fun onPacketReceiving(event: PacketEvent) {
                val player = event.player
                val packet = event.packet
                val entityId = player.entityId

                val list = Ranger.findByEntityId(entityId) ?: return

                val rot = com.github.justadeni.standapi.datatype.Rotation(packet.float.read(1), packet.float.read(0), 0f)

                for (stand in list){
                    stand.setHeadPose(rot)
                }
            }
        })
    }
}
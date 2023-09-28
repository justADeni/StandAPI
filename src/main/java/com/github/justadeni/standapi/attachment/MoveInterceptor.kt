package com.github.justadeni.standapi.attachment

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.github.justadeni.standapi.Misc.sendTo
import com.github.justadeni.standapi.Ranger
import com.github.justadeni.standapi.StandAPI
import org.bukkit.Bukkit

/**
 * @suppress
 */
class MoveInterceptor {
    init {
        //this is for mobs
        //TODO: make another one for players https://wiki.vg/Protocol#Set_Player_Position
        StandAPI.manager().addPacketListener(object : PacketAdapter(StandAPI.plugin(), ListenerPriority.NORMAL, PacketType.Play.Server.REL_ENTITY_MOVE) {
            override fun onPacketSending(event: PacketEvent) {
                val player = event.player
                val packet = event.packet
                val entityId = packet.integers.read(0)

                StandAPI.log("entityId: $entityId")

                val list = Ranger.findByEntityId(entityId) ?: return

                StandAPI.log("list: $list")

                for (stand in list)
                    packet.shallowClone().also { it.integers.write(0, stand.id) }.sendTo(player)
            }
        })
    }
}
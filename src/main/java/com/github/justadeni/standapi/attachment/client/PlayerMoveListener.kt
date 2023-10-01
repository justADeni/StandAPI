package com.github.justadeni.standapi.attachment.client

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.github.justadeni.standapi.Misc.applyOffset
import com.github.justadeni.standapi.Misc.isAnyoneNearby
import com.github.justadeni.standapi.Ranger
import com.github.justadeni.standapi.StandAPI

/**
 * @suppress
 */
class PlayerMoveListener {
    init {
        StandAPI.manager().addPacketListener(object : PacketAdapter(StandAPI.plugin(), ListenerPriority.LOWEST, PacketType.Play.Client.POSITION) {
            override fun onPacketReceiving(event: PacketEvent) {

                /*
                val player = event.player
                val packet = event.packet
                val entityId = player.entityId

                val list = Ranger.findByEntityId(entityId) ?: return

                val loc = Location(player.world, packet.doubles.read(0), packet.doubles.read(1), packet.doubles.read(2))

                for (stand in list){
                    val offsetLoc = loc.applyOffset(stand.getAttached()?.second)
                    //val offsetLoc = player.location.applyOffset(stand.getAttached()?.second)
                    stand.setLocationNoUpdate(offsetLoc)
                    stand.packetGen.teleport(offsetLoc).sendTo(player)
                }
                */
                val player = event.player
                val packet = event.packet
                val entityId = player.entityId

                val list = Ranger.findAttachedTo(entityId) ?: return

                if (player.isAnyoneNearby())
                    return

                for (stand in list){
                    stand.setLocation(player.location.applyOffset(stand.getAttached()?.second))
                }
            }
        })
    }
}
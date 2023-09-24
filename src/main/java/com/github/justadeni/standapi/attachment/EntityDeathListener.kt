package com.github.justadeni.standapi.attachment

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.github.justadeni.standapi.Misc.sendTo
import com.github.justadeni.standapi.Ranger
import com.github.justadeni.standapi.StandAPI

class EntityDeathListener {
    init {
        StandAPI.getManager().addPacketListener(object : PacketAdapter(StandAPI.getPlugin(), ListenerPriority.NORMAL, PacketType.Play.Server.REL_ENTITY_MOVE) {
            override fun onPacketSending(event: PacketEvent) {
                val packet = event.packet
                val ids = packet.intLists.read(0)

                for (entityId in ids){
                    val standList = Ranger.findByEntityId(entityId)
                    if (standList.isEmpty())
                        return

                    for (stand in standList){
                        stand.detachFrom()
                    }
                }
            }
        })
    }
}
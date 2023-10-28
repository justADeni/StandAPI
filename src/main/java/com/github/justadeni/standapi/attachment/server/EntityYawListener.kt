package com.github.justadeni.standapi.attachment.server

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.github.justadeni.standapi.StandManager
import com.github.justadeni.standapi.StandAPI
import com.github.justadeni.standapi.datatype.Rotation

/**
 * @suppress
 */
class EntityYawListener {
    init {
        StandAPI.manager().addPacketListener(object : PacketAdapter(StandAPI.plugin(), ListenerPriority.LOW, PacketType.Play.Server.ENTITY_HEAD_ROTATION) {
            override fun onPacketSending(event: PacketEvent) {
                val packet = event.packet
                val entityId = packet.integers.read(0)

                val list = StandManager.attachedTo(entityId) ?: return

                for (stand in list) {

                    if (stand.isAttachedYaw())
                        stand.setHeadPose(Rotation(stand.getHeadPose().pitch,  packet.bytes.read(0) * 360.0F / 256.0F, stand.getHeadPose().roll))
                }
            }
        })
    }
}
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
 * this packet gives head pitch and body yaw
 * for head yaw, ENTITY_HEAD_ROTATION is used
 */
class EntityPitchListener {
    init {
        StandAPI.manager().addPacketListener(object : PacketAdapter(StandAPI.plugin(), ListenerPriority.LOW, PacketType.Play.Server.ENTITY_LOOK) {
            override fun onPacketSending(event: PacketEvent) {
                //val player = event.player
                val packet = event.packet
                val entityId = packet.integers.read(0)

                val list = StandManager.attachedTo(entityId) ?: return

                for (stand in list) {

                    if (stand.isAttachedPitch())
                        stand.setHeadPose(Rotation(packet.bytes.read(1) * 360.0F / 256.0F, stand.getHeadPose().yaw, stand.getHeadPose().roll))
                }
            }
        })
    }
}
package com.github.justadeni.standapi.attachment.client

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.github.justadeni.standapi.Misc.isAnyoneNearby
import com.github.justadeni.standapi.StandManager
import com.github.justadeni.standapi.StandAPI
import com.github.justadeni.standapi.datatype.Rotation

/**
 * @suppress
 */
class PlayerRotListener {
    init {
        StandAPI.manager().addPacketListener(object : PacketAdapter(StandAPI.plugin(), ListenerPriority.LOWEST, PacketType.Play.Client.LOOK) {
            override fun onPacketReceiving(event: PacketEvent) {
                /*
                val player = event.player
                val packet = event.packet
                val entityId = player.entityId

                val list = Ranger.findByEntityId(entityId) ?: return

                val rot = com.github.justadeni.standapi.datatype.Rotation(packet.float.read(1), packet.float.read(0), 0f)

                for (stand in list){
                    stand.rotations[0] = rot
                    val pitchPacket = StandAPI.manager().createPacket(PacketType.Play.Server.ENTITY_LOOK)
                    pitchPacket.integers.write(0, stand.id)
                    pitchPacket.bytes.write(0, (stand.getBodyPose().yaw * 256.0F / 360.0F).toInt().toByte())
                    pitchPacket.bytes.write(1, (stand.getHeadPose().pitch * 256.0F / 360.0F).toInt().toByte())
                    pitchPacket.sendTo(player)

                    val yawPacket = StandAPI.manager().createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION)
                    yawPacket.integers.write(0, stand.id)
                    yawPacket.bytes.write(0, stand.getHeadPose().yaw.toInt().toByte())
                    yawPacket.sendTo(player)
                }
                */

                val player = event.player
                //val packet = event.packet
                val entityId = player.entityId

                val list = StandManager.findAttachedTo(entityId) ?: return

                if (player.isAnyoneNearby())
                    return

                for (stand in list){
                    stand.setHeadPose(Rotation(player.location.pitch, player.location.yaw, 0f))
                }
            }
        })
    }
}
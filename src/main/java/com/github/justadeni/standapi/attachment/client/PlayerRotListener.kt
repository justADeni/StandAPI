package com.github.justadeni.standapi.attachment.client

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.github.justadeni.standapi.Misc.sendTo
import com.github.justadeni.standapi.Ranger
import com.github.justadeni.standapi.StandAPI

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
                    stand.rotations[0] = rot
                    val pitchPacket = PacketContainer(PacketType.Play.Server.ENTITY_LOOK)
                    pitchPacket.integers.write(0, stand.id)
                    pitchPacket.bytes.write(0, (stand.getBodyPose().yaw * 256.0F / 360.0F).toInt().toByte())
                    pitchPacket.bytes.write(1, (stand.getHeadPose().pitch * 256.0F / 360.0F).toInt().toByte())
                    pitchPacket.sendTo(player)

                    val yawPacket = PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION)
                    yawPacket.integers.write(0, stand.id)
                    yawPacket.bytes.write(0, stand.getHeadPose().yaw.toInt().toByte())
                    yawPacket.sendTo(player)
                }
            }
        })
    }
}
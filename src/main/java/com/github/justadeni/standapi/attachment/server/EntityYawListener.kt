package com.github.justadeni.standapi.attachment.server

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.github.justadeni.standapi.Misc.sendTo
import com.github.justadeni.standapi.Ranger
import com.github.justadeni.standapi.StandAPI
import com.github.justadeni.standapi.datatype.Rotation
import org.bukkit.Location

class EntityYawListener {
    init {
        StandAPI.manager().addPacketListener(object : PacketAdapter(StandAPI.plugin(), ListenerPriority.LOW, PacketType.Play.Server.ENTITY_HEAD_ROTATION) {
            override fun onPacketSending(event: PacketEvent) {
                val player = event.player
                val packet = event.packet
                val entityId = packet.integers.read(0)

                val list = Ranger.findByEntityId(entityId) ?: return

                for (stand in list) {
                    val cloned = packet.shallowClone()

                    if (stand.isAttachedYaw())
                        stand.rotations[0] = Rotation(stand.getHeadPose().pitch, cloned.bytes.read(0).toFloat(), stand.getHeadPose().roll)
                    else
                        cloned.bytes.write(0, stand.getHeadPose().yaw.toInt().toByte())

                    cloned.integers.write(0, stand.id)
                    cloned.sendTo(player)
                }
            }
        })
    }
}
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
                //val player = event.player
                val packet = event.packet
                val entityId = packet.integers.read(0)

                val list = StandManager.findAttachedTo(entityId) ?: return

                //val attachedToPlayer = Misc.getPlayerById(entityId, player.world)

                for (stand in list) {
                    /*
                    val cloned = packet.shallowClone()
                    cloned.integers.write(0, stand.id)

                    if (stand.isAttachedYaw())
                        stand.rotations[0] = Rotation(stand.getHeadPose().pitch, cloned.bytes.read(0) * 360.0F / 256.0F, stand.getHeadPose().roll)
                    else
                        continue

                    cloned.sendTo(player)
                    if (attachedToPlayer != null)
                        cloned.sendTo(attachedToPlayer)
                    */

                    if (stand.isAttachedYaw())
                        stand.setHeadPose(Rotation(stand.getHeadPose().pitch,  packet.bytes.read(0) * 360.0F / 256.0F, stand.getHeadPose().roll))
                }
            }
        })
    }
}
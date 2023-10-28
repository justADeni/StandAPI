package com.github.justadeni.standapi.attachment.server

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.github.justadeni.standapi.misc.Util
import com.github.justadeni.standapi.misc.Util.applyOffset
import com.github.justadeni.standapi.misc.Util.sendTo
import com.github.justadeni.standapi.StandManager
import com.github.justadeni.standapi.StandAPI
import com.github.justadeni.standapi.datatype.Rotation

/**
 * @suppress
 */
class EntityPitchMoveListener {

    init {
        StandAPI.manager().addPacketListener(object : PacketAdapter(StandAPI.plugin(), ListenerPriority.LOW, PacketType.Play.Server.REL_ENTITY_MOVE_LOOK) {
            override fun onPacketSending(event: PacketEvent) {
                val player = event.player
                val packet = event.packet
                val entityId = packet.integers.read(0)

                val list = StandManager.attachedTo(entityId) ?: return

                val attachedToPlayer = Util.getPlayerById(entityId, player.world)

                for (stand in list) {

                    stand.setLocationNoUpdate(player.location.applyOffset(stand.getAttached()?.second))

                    val cloned = packet.shallowClone()
                    cloned.bytes.write(0, (stand.getBodyPose().yaw * 256.0F / 360.0F).toInt().toByte())
                    cloned.integers.write(0, stand.id)
                    cloned.sendTo(player)

                    if (attachedToPlayer != null)
                        cloned.sendTo(attachedToPlayer)

                    if (stand.isAttachedPitch())
                        stand.setHeadPose(Rotation(packet.bytes.read(1) * 360.0F / 256.0F, stand.getHeadPose().yaw, stand.getHeadPose().roll))
                }
            }
        })
    }
}
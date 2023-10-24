package com.github.justadeni.standapi.attachment.client

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.github.justadeni.standapi.misc.Util.isAnyoneNearby
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
                val player = event.player
                val entityId = player.entityId

                val list = StandManager.attachedTo(entityId) ?: return

                if (player.isAnyoneNearby())
                    return

                for (stand in list){

                    val pitch = if (stand.isAttachedPitch()) player.location.pitch else stand.getHeadPose().pitch
                    val yaw = if (stand.isAttachedYaw()) player.location.yaw else stand.getHeadPose().yaw

                    stand.setHeadPose(Rotation(pitch, yaw, 0f))
                }
            }
        })
    }
}
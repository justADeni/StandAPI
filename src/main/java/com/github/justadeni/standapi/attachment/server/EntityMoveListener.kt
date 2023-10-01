package com.github.justadeni.standapi.attachment.server

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.github.justadeni.standapi.Misc
import com.github.justadeni.standapi.Misc.applyOffset
import com.github.justadeni.standapi.Misc.sendTo
import com.github.justadeni.standapi.Ranger
import com.github.justadeni.standapi.StandAPI
import org.bukkit.Location

/**
 * @suppress
 */
class EntityMoveListener {
    init {
        StandAPI.manager().addPacketListener(object : PacketAdapter(StandAPI.plugin(), ListenerPriority.LOW, PacketType.Play.Server.REL_ENTITY_MOVE) {
            override fun onPacketSending(event: PacketEvent) {
                val player = event.player
                val packet = event.packet
                val entityId = packet.integers.read(0)

                StandAPI.log("entity moved: $entityId")
                //TODO: check if player not excluded in every stands
                //TODO: send to player if they are the entity

                val list = Ranger.findByEntityId(entityId) ?: return

                val attachedToPlayer = Misc.getPlayerById(entityId, player.world)

                for (stand in list) {
                    val cloned = packet.shallowClone()
                    //stand.setLocationNoUpdate(Location(player.world, cloned.doubles.read(0), cloned.doubles.read(1), cloned.doubles.read(2)))
                    stand.setLocationNoUpdate(player.location.applyOffset(stand.getAttached()?.second))
                    cloned.integers.write(0, stand.id)
                    cloned.sendTo(player)

                    if (attachedToPlayer != null)
                        cloned.sendTo(attachedToPlayer)
                }
            }
        })
    }
}
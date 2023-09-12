package com.github.justadeni.standapi.event

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers
import com.github.justadeni.standapi.Misc
import com.github.justadeni.standapi.Ranger
import com.github.justadeni.standapi.StandAPI
import com.github.justadeni.standapi.StandAPI.Companion.getManager
import com.github.justadeni.standapi.StandAPI.Companion.getPlugin
import com.github.shynixn.mccoroutine.bukkit.launch
import org.bukkit.Bukkit

class Interceptor {
    init {
        getManager().addPacketListener(object : PacketAdapter(getPlugin(), ListenerPriority.NORMAL, PacketType.Play.Client.USE_ENTITY) {
            override fun onPacketReceiving(event: PacketEvent) {
                val player = event.player
                val packet = event.packet
                val id = packet.integers.read(0)
                val stand = Ranger.find(id)

                val action = when (packet.enumEntityUseActions.read(0).action){
                    EnumWrappers.EntityUseAction.ATTACK -> Action.LEFT_CLICK
                    EnumWrappers.EntityUseAction.INTERACT -> Action.RIGHT_CLICK
                    EnumWrappers.EntityUseAction.INTERACT_AT -> Action.RIGHT_CLICK
                }

                if (id > 9999 && id <= Misc.currentID())
                    StandAPI.getPlugin().launch { Bukkit.getPluginManager().callEvent(PacketStandEvent(player, id, stand, action)) }
            }
        })
    }
}

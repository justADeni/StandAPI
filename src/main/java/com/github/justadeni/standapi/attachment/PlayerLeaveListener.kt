package com.github.justadeni.standapi.attachment

import com.github.justadeni.standapi.StandAPI
import com.github.justadeni.standapi.StandManager
import com.github.shynixn.mccoroutine.bukkit.launch
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerLeaveListener: Listener {

    @EventHandler
    fun onPlayerLeave(e: PlayerQuitEvent){
        StandManager.removeOfflineIncluded(e.player)

        val list = StandManager.attachedTo(e.player.entityId) ?: return
        for (stand in list) {
            StandManager.remove(stand)
            StandManager.addWithId(stand, -1)
        }
    }
}
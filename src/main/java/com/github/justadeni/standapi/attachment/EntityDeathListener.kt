package com.github.justadeni.standapi.attachment

import com.github.justadeni.standapi.StandAPI
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import kotlinx.coroutines.withContext
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

class EntityDeathListener: Listener {
    @EventHandler
    suspend fun onEntityDeath(e: EntityDeathEvent) = withContext(StandAPI.getPlugin().asyncDispatcher){
        if (Attacher.getMap().containsKey(e.entity.uniqueId))
            Attacher.removeKey(e.entity.entityId)
    }
}
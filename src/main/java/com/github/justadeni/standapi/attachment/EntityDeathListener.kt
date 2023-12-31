package com.github.justadeni.standapi.attachment

import com.github.justadeni.standapi.StandManager
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent

/**
 * @suppress
 */
class EntityDeathListener: Listener {

    @EventHandler
    fun onEntityDeath(e: EntityDeathEvent){
        if (e.entityType == EntityType.PLAYER)
            return

        val list = StandManager.attachedTo(e.entity.entityId) ?: return

        for (stand in list){
            stand.detachFrom()
        }
    }
}
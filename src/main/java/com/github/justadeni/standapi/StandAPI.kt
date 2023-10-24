package com.github.justadeni.standapi

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.github.justadeni.standapi.attachment.EntityDeathListener
import com.github.justadeni.standapi.attachment.client.PlayerMoveListener
import com.github.justadeni.standapi.attachment.client.PlayerRotListener
import com.github.justadeni.standapi.attachment.client.PlayerRotMoveListener
import com.github.justadeni.standapi.attachment.server.*
import com.github.justadeni.standapi.event.UseEntityListener
import com.github.justadeni.standapi.misc.Util
import com.github.justadeni.standapi.storage.Saver
import com.github.justadeni.standapi.testing.Command
import com.github.justadeni.standapi.testing.TabComplete
import com.github.shynixn.mccoroutine.bukkit.*
import org.bukkit.plugin.java.JavaPlugin

/**
 * @suppress
 */
class StandAPI : SuspendingJavaPlugin() {

    companion object {

        private var plugin: JavaPlugin? = null

        private var manager: ProtocolManager? = null

        internal fun plugin(): JavaPlugin {
            return plugin!!
        }

        internal fun manager(): ProtocolManager {
            return manager!!
        }
    }

    override suspend fun onLoadAsync() {
        plugin = this
        manager = ProtocolLibrary.getProtocolManager()
    }

    override suspend fun onEnableAsync() {

        //only for testing
        //getCommand("standapi")!!.setSuspendingExecutor(Command())
        //getCommand("standapi")!!.setSuspendingTabCompleter(TabComplete())

        server.pluginManager.registerSuspendingEvents(EntityDeathListener(), this)
        UseEntityListener()
        EntityMoveListener()
        EntityPitchListener()
        EntityPitchMoveListener()
        EntityYawListener()
        TeleportListener()

        PlayerMoveListener()
        PlayerRotListener()
        PlayerRotMoveListener()
        Saver.loadAll()
        Util.resetId()
        launch { StandManager.startTicking() }
        Util.initilazeTrackingRanges()
        Saver.tickingSaving()
        Metrics(this, 19953)
    }

    override fun onDisable() {
        Saver.saveAll()
    }
}

package com.github.justadeni.standapi

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.github.justadeni.standapi.attachment.EntityDeathListener
import com.github.justadeni.standapi.attachment.MoveInterceptor
import com.github.justadeni.standapi.attachment.RotMoveInterceptor
import com.github.justadeni.standapi.attachment.TeleportInterceptor
import com.github.justadeni.standapi.storage.StandApiConfig
import com.github.justadeni.standapi.event.UseEntityInterceptor
import com.github.justadeni.standapi.storage.Saver
import com.github.justadeni.standapi.testing.TabComplete
import com.github.justadeni.standapi.testing.Command
import com.github.shynixn.mccoroutine.bukkit.*
import kotlinx.coroutines.runBlocking
import org.bukkit.plugin.java.JavaPlugin

/**
 * @suppress
 */
class StandAPI : SuspendingJavaPlugin() {

    companion object {

        private var plugin: JavaPlugin? = null

        private var manager: ProtocolManager? = null

        fun plugin(): JavaPlugin {
            return plugin!!
        }

        fun manager(): ProtocolManager {
            return manager!!
        }

        fun log(info: String){
            plugin!!.logger.info(info)
        }
    }

    override suspend fun onLoadAsync() {
        plugin = this
        manager = ProtocolLibrary.getProtocolManager()
        saveDefaultConfig()
        StandApiConfig.reload()
    }

    override suspend fun onEnableAsync() {
        getCommand("standapi")!!.setSuspendingExecutor(Command())
        getCommand("standapi")!!.setSuspendingTabCompleter(TabComplete())
        UseEntityInterceptor()
        EntityDeathListener()
        MoveInterceptor()
        RotMoveInterceptor()
        TeleportInterceptor()
        Saver.loadAll()
        Misc.resetId()
        launch { Ranger.startTicking() }
    }

    override fun onDisable() {
        Saver.saveAll()
    }
}

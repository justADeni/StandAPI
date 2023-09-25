package com.github.justadeni.standapi

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.github.justadeni.standapi.attachment.EntityDeathListener
import com.github.justadeni.standapi.attachment.MoveInterceptor
import com.github.justadeni.standapi.attachment.RotMoveInterceptor
import com.github.justadeni.standapi.attachment.TeleportInterceptor
import com.github.justadeni.standapi.storage.Config
import com.github.justadeni.standapi.event.UseEntityInterceptor
import com.github.justadeni.standapi.storage.Saver
import com.github.justadeni.standapi.testing.TabComplete
import com.github.justadeni.standapi.testing.Command
import com.github.shynixn.mccoroutine.bukkit.*
import org.bukkit.plugin.java.JavaPlugin

class StandAPI : SuspendingJavaPlugin() {

    companion object {

        private var plugin: JavaPlugin? = null

        private var manager: ProtocolManager? = null

        fun getPlugin(): JavaPlugin {
            return plugin!!
        }

        fun getManager(): ProtocolManager {
            return manager!!
        }
    }

    override suspend fun onLoadAsync() {
        plugin = this
        manager = ProtocolLibrary.getProtocolManager()
        saveDefaultConfig()
        Config.reload()
    }

    override suspend fun onEnableAsync() {
        getCommand("standapi")!!.setSuspendingExecutor(Command())
        getCommand("standapi")!!.setSuspendingTabCompleter(TabComplete())
        EntityDeathListener()
        UseEntityInterceptor()
        MoveInterceptor()
        RotMoveInterceptor()
        TeleportInterceptor()
        Saver.loadAll()
        launch { Ranger.startTicking() }
    }

    override suspend fun onDisableAsync() {
        Saver.saveAll()
    }
}

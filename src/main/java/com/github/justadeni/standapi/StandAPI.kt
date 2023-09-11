package com.github.justadeni.standapi

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.launch
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
    }

    override suspend fun onEnableAsync() {
        getCommand("standapi")!!.setExecutor(com.github.justadeni.standapi.testing.Command())
        launch { Ranger.tick() }
    }


    override suspend fun onDisableAsync() {

    }
}

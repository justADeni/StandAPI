package com.github.justadeni.standapi

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import org.bukkit.plugin.java.JavaPlugin

class StandAPI : JavaPlugin() {

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

    override fun onLoad() {
        plugin = this
        manager = ProtocolLibrary.getProtocolManager()
    }

    override fun onDisable() {
        plugin = null
    }
}

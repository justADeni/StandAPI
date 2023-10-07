package com.github.justadeni.standapi

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.github.justadeni.standapi.Misc.squared
import com.github.justadeni.standapi.attachment.EntityDeathListener
import com.github.justadeni.standapi.attachment.client.PlayerMoveListener
import com.github.justadeni.standapi.attachment.client.PlayerRotListener
import com.github.justadeni.standapi.attachment.client.PlayerRotMoveListener
import com.github.justadeni.standapi.attachment.server.*
import com.github.justadeni.standapi.event.UseEntityListener
import com.github.justadeni.standapi.storage.Saver
import com.github.justadeni.standapi.testing.TabComplete
import com.github.justadeni.standapi.testing.Command
import com.github.shynixn.mccoroutine.bukkit.*
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin
import org.spigotmc.SpigotWorldConfig
import java.util.*

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

        internal fun log(info: String){
            plugin!!.logger.info(info)
        }

        private val pTrackingRanges = hashMapOf<UUID, Int>()

        internal fun getPTrackingRange2(worlduuid: UUID): Int {
            return pTrackingRanges[worlduuid] ?: 2304 //48
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
        Misc.resetId()
        launch { StandManager.startTicking() }
        for (world in Bukkit.getWorlds()){
            pTrackingRanges[world.uid] = SpigotWorldConfig(world.name).playerTrackingRange.squared()
        }
        Saver.tickingSaving()
        Metrics(this, 19953)
    }

    override fun onDisable() {
        Saver.saveAll()
    }
}

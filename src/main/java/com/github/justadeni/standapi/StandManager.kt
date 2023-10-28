package com.github.justadeni.standapi

import com.github.justadeni.standapi.misc.Logger
import com.github.justadeni.standapi.misc.Util.applyOffset
import com.github.justadeni.standapi.misc.Util.sendTo
import com.github.justadeni.standapi.misc.Util.squared
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.*
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

object StandManager {

    /*
    for all existing stands
    first Int is entity Id of the entity they're bound to
    */
    private val ticking = ConcurrentHashMap<Int, MutableList<PacketStand>>()

    /*
    for stands that are within detection radius of player
    and have received packets already
    */
    private val included = ConcurrentHashMap<Player, MutableList<PacketStand>>()

    /**
     * returns list of all stands on the server
     */
    @JvmStatic
    fun all(): List<PacketStand> {
        return ticking.values.flatten()
    }

    /**
     * returns list of all stands on the server with specified plugin name
     * @param pluginName name of plugin at PacketStand creation
     */
    @JvmStatic
    fun ofPlugin(pluginName: String): List<PacketStand> {
        return all().groupBy { it.pluginName }[pluginName] ?: emptyList()
    }

    /**
     * returns list of all stands in specified world
     * @param world in which stands will be retrieved
     */
    @JvmStatic
    fun inWorld(world: World): List<PacketStand> {
            return all().filter { it.getLocation().world == world }
    }

    /**
     * returns stand with that id or null if not found
     * @param standId of the stand
     */
    @JvmStatic
    fun byId(standId: Int): PacketStand? {
        return all().firstOrNull { it.id == standId }
    }

    /**
     * returns list of stands attached to that entity or null if empty
     * will also return null if entity is an offline player
     * @param entityId of entity with attached stands
     */
    @JvmStatic
    fun attachedTo(entityId: Int): List<PacketStand>? {
        return ticking[entityId]?.toList()
    }

    internal fun add(stand: PacketStand){
        if (stand.getAttached() == null){
            addWithId(stand, -2)
            return
        }

        val entityId = Bukkit.getEntity(stand.getAttached()!!.first)?.entityId

        if (entityId == null){
            addWithId(stand, -1)
            return
        }

        addWithId(stand, entityId)
    }

    //only use when you're 100% sure
    internal fun addWithId(stand: PacketStand, id: Int) {
        if (ticking.containsKey(id)) {
            if (!ticking[id]!!.contains(stand)) {
                ticking[id]!!.add(stand)
            }
        }else {
            ticking[id] = Collections.synchronizedList(mutableListOf(stand))
        }
    }

    internal fun remove(stand: PacketStand) {
        for (entry in ticking.entries.toList()){
            if (!entry.value.contains(stand))
                continue

            if (!entry.value.remove(stand))
                continue

            if (entry.value.isEmpty())
                ticking.remove(entry.key)

            break
        }
    }

    internal fun removeOfflineIncluded(player: Player){
        included.remove(player)
    }

    internal suspend fun startTicking() = withContext(StandAPI.plugin().asyncDispatcher){
        while (true) {

            Logger.debug("-                ")
            Logger.debug("ticking: $ticking")
            Logger.debug("                  ")
            Logger.debug("included: $included")
            Logger.debug("--                    ")

            delay(20.ticks)

            for (player in Bukkit.getOnlinePlayers().toList()) {

                if (!included.containsKey(player))
                    included[player] = mutableListOf()

                val wereInside = included[player]!!

                val areInside = all().asSequence()
                    .filter { it.getLocation().world == player.world }
                    .filterNot { it.excludedUUIDs().contains(player.uniqueId) }
                    .filter { it.getLocation().distanceSquared(player.location) < 192.squared() }
                    .toList()

                val wentInside = areInside - wereInside.toSet()
                wentInside.forEach {
                    it.packetBundle.sendTo(player)
                }

                included[player] = areInside.toMutableList()

                val wentOutside = wereInside - areInside.toSet()
                wentOutside.forEach {
                    it.destroyPacket.sendTo(player)
                }

            }

            if (!ticking.containsKey(-1))
                continue


            val listIt = ticking[-1]!!.iterator()
            while (listIt.hasNext()) {
                val stand = listIt.next()

                stand.getAttached() ?: continue
                val entity = withContext(StandAPI.plugin().minecraftDispatcher) { Bukkit.getEntity(stand.getAttached()!!.first) } ?: continue

                listIt.remove()

                addWithId(stand, entity.entityId)
                stand.setLocation(entity.location.applyOffset(stand.getAttached()?.second))
            }
        }
    }

}
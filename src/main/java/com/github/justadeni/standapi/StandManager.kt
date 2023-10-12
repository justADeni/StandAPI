package com.github.justadeni.standapi

import com.github.justadeni.standapi.Misc.applyOffset
import com.github.justadeni.standapi.Misc.sendTo
import com.github.justadeni.standapi.Misc.squared
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player

object StandManager {

    private val mutex = Mutex()
    /*
    for all existing stands
    first Int is entity Id of the entity they're bound to
    */
    private val ticking = hashMapOf<Int, MutableList<PacketStand>>()

    /*
    for stands that are within detection radius of player
    and have received packets already
    */
    private val included = hashMapOf<Player, MutableList<PacketStand>>()

    /**
     * returns list of all stands on the server
     * use with care if other plugin uses StandAPI on the same server
     */
    @JvmStatic
    fun getAllStands(): List<PacketStand> {
        //val wholeList = mutableListOf<PacketStand>()
        return runBlocking {
            val wholeList = mutableListOf<PacketStand>()
            mutex.withLock {
                ticking.values.forEach { wholeList.addAll(it) }
            }
            return@runBlocking wholeList
        }
    }

    internal suspend fun getAllStandsSuspend(): List<PacketStand> {
        val wholeList = mutableListOf<PacketStand>()
        mutex.withLock {
            ticking.values.forEach { wholeList.addAll(it) }
        }
        return wholeList
    }

    /**
     * returns list of all stands on the server with specified plugin name
     * @param pluginName name of plugin at PacketStand instantiation
     */
    @JvmStatic
    fun getStandsOfPlugin(pluginName: String): List<PacketStand> {
        return getAllStands().groupBy { it.pluginName }[pluginName] ?: emptyList()
    }

    /**
     * returns list of all stands in specified world
     * @param world in which stands will be retrieved
     * use with care if other plugin uses StandAPI on the same server
     */
    @JvmStatic
    fun getStandsInWorld(world: World): List<PacketStand> {
        return getAllStands().filter { it.getLocation().world == world }
    }

    /**
     * returns list of stands attached to that entity or null if empty
     * will also return null if entity is an offline player
     * @param entityId of entity with attached stands
     */
    @JvmStatic
    fun findAttachedTo(entityId: Int): List<PacketStand>? {
        //if (ticking.containsKey(entityId))
        return ticking[entityId]

        //return emptyList()
    }

    internal fun findByStandId(standId: Int): PacketStand? {
        for (list in ticking.values)
            for (stand in list)
                if (stand.id == standId)
                    return stand

        return null
    }

    internal suspend fun findByStandIdSuspend(standId: Int): PacketStand? = mutex.withLock {
        for (list in ticking.values)
            for (stand in list)
                if (stand.id == standId)
                    return stand

        return null
    }

    internal suspend fun add(stand: PacketStand){
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
    private suspend fun addWithId(stand: PacketStand, id: Int) = mutex.withLock {
        if (ticking.containsKey(id)) {
            if (!ticking[id]!!.contains(stand)) {
                ticking[id]!!.add(stand)
            }
        }else {
            ticking[id] = mutableListOf(stand)
        }
    }

    internal suspend fun remove(stand: PacketStand) = mutex.withLock {
        val mapIt = ticking.entries.iterator()
        while (mapIt.hasNext()){
            val pair = mapIt.next()
            if (!pair.value.contains(stand))
                continue

            pair.value.remove(stand)
            if (pair.value.isEmpty())
                mapIt.remove()
            break
        }
    }

    internal suspend fun startTicking() = withContext(StandAPI.plugin().asyncDispatcher){
        while (true) {
            delay(20.ticks)

            val allStands = getAllStandsSuspend()

            for (player in Bukkit.getOnlinePlayers()) {

                if (!included.containsKey(player))
                    included[player] = mutableListOf()

                val wereInside = included[player]!!

                val areInside = allStands.asSequence()
                    .filterNot { it.excludedUUIDs().contains(player.uniqueId) }
                    .filter { it.getLocation().world == player.world }
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


            if (mutex.withLock { return@withLock !ticking.containsKey(-1) })
                continue

            val listIt = mutex.withLock { ticking[-1]!!.iterator() }
            while (listIt.hasNext()) {
                val stand = listIt.next()

                stand.getAttached() ?: continue
                val pE =
                    withContext(StandAPI.plugin().minecraftDispatcher) { Bukkit.getEntity(stand.getAttached()!!.first) }
                        ?: continue

                mutex.withLock {
                    listIt.remove()
                }

                addWithId(stand, pE.entityId)
                stand.setLocation(pE.location.applyOffset(stand.getAttached()?.second))
            }

        }
    }

}
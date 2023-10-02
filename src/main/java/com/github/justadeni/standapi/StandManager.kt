package com.github.justadeni.standapi

import com.github.justadeni.standapi.Misc.applyOffset
import com.github.justadeni.standapi.Misc.sendTo
import com.github.justadeni.standapi.storage.Saver
import com.github.justadeni.standapi.storage.StandApiConfig
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.*

object StandManager {
    /*
    for all existing stands
    first Int is entity Id of the entity they're bound to
    */
    private val ticking = hashMapOf<Int, MutableList<PacketStand>>()

    /*
    for stands that are within detection radius of player
    and have received packets already
    */
    private val included = Collections.synchronizedMap(hashMapOf<Player, MutableList<PacketStand>>())

    /**
     * returns list of all stands on the server
     */
    @JvmStatic
    fun getAllStands(): List<PacketStand>{
        val wholeList = mutableListOf<PacketStand>()
        ticking.values.forEach { wholeList.addAll(it) }
        return wholeList
    }

    /**
     * returns list of all stands in specified world
     * @param world in which stands will be retrieved
     */
    @JvmStatic
    fun getAllStandsInWorld(world: World): List<PacketStand>{
        return getAllStands().filter { it.getLocation().world == world }
    }

    /**
     * returns list of stands attached to that entity or null if empty
     * will also return null if entity is an offline player
     * @param entityId of entity with attached stands
     */
    @JvmStatic
    fun findAttachedTo(entityId: Int): List<PacketStand>? {
        return ticking[entityId]
    }

    internal fun findByStandId(standId: Int): PacketStand? {
        for (list in ticking.values)
            for (stand in list)
                if (stand.id == standId)
                    return stand

        return null
    }

    internal fun add(stand: PacketStand) = StandAPI.plugin().launch{
        if (stand.getAttached() == null){
            addWithId(stand, -2)
            return@launch
        }

        val entityId = Bukkit.getEntity(stand.getAttached()!!.first)?.entityId

        if (entityId == null){
            addWithId(stand, -1)
            return@launch
        }

        addWithId(stand, entityId)
    }

    //only use when you're 100% sure
    private fun addWithId(stand: PacketStand, id: Int){
        if (ticking.containsKey(id)) {
            if (!ticking[id]!!.contains(stand)) {
                ticking[id]!!.add(stand)
            }
        }else {
            ticking[id] = mutableListOf(stand)
        }
    }

    internal fun remove(stand: PacketStand){
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

            val allStands = getAllStands()

            for (player in Bukkit.getOnlinePlayers()){
                launch {
                    if (!included.containsKey(player))
                        included[player] = mutableListOf()

                    val wereInside = included[player]!!

                    val areInside = async{ allStands//.asSequence()
                        .filterNot { it.excludedUUIDs().contains(player.uniqueId) }
                        .filter { it.getLocation().world == player.world }
                        .filter { it.getLocation().distanceSquared(player.location) < StandApiConfig.getRenderDistance2() }
                    }.await()
                    //.toList()
                    included[player] = areInside.toMutableList()

                    val wentInside = async { areInside - wereInside.toSet() }
                    val wentOutside = async { wereInside - areInside.toSet() }

                    wentInside.await().forEach {
                        it.packetBundle.sendTo(player)
                    }
                    wentOutside.await().forEach {
                        it.destroyPacket.sendTo(player)
                    }
                }
            }

            if (!ticking.containsKey(-1))
                continue

            val listIt = ticking[-1]!!.iterator()
            while (listIt.hasNext()){
                val stand = listIt.next()
                /*
                if (stand.getAttached() == null){
                    listIt.remove()
                    addWithId(stand, -2)
                    continue
                }
                */
                stand.getAttached() ?: continue
                val pE = async(StandAPI.plugin().minecraftDispatcher){ Bukkit.getEntity(stand.getAttached()!!.first) }.await() ?: continue
                listIt.remove()
                addWithId(stand, pE.entityId)
                stand.setLocation(pE.location.applyOffset(stand.getAttached()?.second))
            }
        }
    }

}
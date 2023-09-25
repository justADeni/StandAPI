package com.github.justadeni.standapi

import com.github.justadeni.standapi.Misc.sendTo
import com.github.justadeni.standapi.storage.StandApiConfig
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.entity.Player

/**
 * @suppress
 */
object Ranger {
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

    internal suspend fun getAllStands(): List<PacketStand> = withContext(StandAPI.plugin().asyncDispatcher) {
        val wholeList = mutableListOf<PacketStand>()
        ticking.values.forEach { wholeList.addAll(it) }
        return@withContext wholeList
    }

    internal fun findByEntityId(entityId: Int): List<PacketStand> {
        if (ticking.containsKey(entityId))
            return ticking[entityId]!!

        return emptyList()
    }

    internal fun findByStandId(standId: Int): PacketStand? {
        for (list in ticking.values)
            for (stand in list)
                if (stand.id == standId)
                    return stand

        return null
    }

    internal fun add(stand: PacketStand){
        add(-1, stand)
    }

    internal fun add(entityId: Int, stand: PacketStand){
        if (ticking.contains(entityId))
            ticking[entityId]!!.add(stand)
        else
            ticking[entityId] = mutableListOf(stand)

        //stand.packetBundle.sendTo(stand.eligiblePlayers())
    }

    internal fun remove(stand: PacketStand){
        val tickingIt = ticking.keys.iterator()
        while (tickingIt.hasNext()){
            val entityId = tickingIt.next()
            if(ticking[entityId]!!.contains(stand)){
                ticking[entityId]!!.remove(stand)
            }
            if (ticking[entityId]!!.isEmpty()){
                tickingIt.remove()
                break
            }
        }
    }

    internal suspend fun startTicking() = withContext(StandAPI.plugin().asyncDispatcher){
        while (true) {
            val allStands = getAllStands()

            for (player in Bukkit.getOnlinePlayers()){

                if (!included.containsKey(player))
                    included[player] = mutableListOf()

                val wereInside = included[player]!!

                val areInside = allStands.asSequence()
                    .filterNot { it.excludedUUIDs().contains(player.uniqueId) }
                    .filter { it.getLocation().world == player.world }
                    .filter { it.getLocation().distanceSquared(player.location) < StandApiConfig.renderDistance2 }
                    .toList()

                val wentInside = areInside - wereInside.toSet()
                wentInside.forEach {
                    it.packetBundle.sendTo(player)
                }
                included[player] = wentInside.toMutableList()

                val wentOutside = wereInside - areInside.toSet()
                wentOutside.forEach {
                    it.destroyPacket.sendTo(player)
                }
            }
        }
    }

}
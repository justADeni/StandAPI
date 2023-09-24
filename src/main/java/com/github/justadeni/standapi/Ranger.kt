package com.github.justadeni.standapi

import com.github.justadeni.standapi.Misc.sendTo
import com.github.justadeni.standapi.storage.Config
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.entity.Player

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

    internal fun getAllStands(): List<PacketStand> {
        val wholeList = mutableListOf<PacketStand>()
        ticking.values.forEach { wholeList.addAll(it) }
        return wholeList
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
        val it = ticking.keys.iterator()
        while (it.hasNext()){
            val entityId = it.next()
            if(ticking[entityId]!!.contains(stand)){
                ticking[entityId]!!.remove(stand)
            }
            if (ticking[entityId]!!.isEmpty()){
                it.remove()
                break
            }
        }
    }

    internal suspend fun startTicking() = withContext(StandAPI.getPlugin().asyncDispatcher){
        while (true) {
            val allStands = getAllStands()

            for (player in Bukkit.getOnlinePlayers()){

                if (!included.containsKey(player))
                    included[player] = mutableListOf()

                val wereInside = included[player]!!

                val areInside = allStands.asSequence()
                    .filterNot { it.excludedUUIDs().contains(player.uniqueId) }
                    .filter { it.getLocation().world == player.world }
                    .filter { it.getLocation().distanceSquared(player.location) < Config.renderDistance2 }
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
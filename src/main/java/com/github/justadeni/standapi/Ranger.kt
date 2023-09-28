package com.github.justadeni.standapi

import com.github.justadeni.standapi.Misc.sendTo
import com.github.justadeni.standapi.storage.StandApiConfig
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.Location
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

    internal fun getAllStands(): List<PacketStand>{
        val wholeList = mutableListOf<PacketStand>()
        ticking.values.forEach { wholeList.addAll(it) }
        return wholeList
    }

    internal fun findByEntityId(entityId: Int): List<PacketStand>? {
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

    internal fun add(stand: PacketStand){
        if (stand.getAttached() == null){
            addWithId(stand, -2)

            StandAPI.log("attached null")

            return
        }

        val entityId = Bukkit.getEntity(stand.getAttached()!!.first)?.entityId
        StandAPI.log("found entity Id: $entityId")

        if (entityId == null){
            stand.detachFrom()
            addWithId(stand, -1)

            StandAPI.log("entityId null")
            return
        }

        StandAPI.log("added stand")
        addWithId(stand, entityId)

        StandAPI.log("list: $ticking")
    }

    //only use when you're 100% sure
    private fun addWithId(stand: PacketStand, id: Int){
        if (ticking.containsKey(id))
            if (!ticking[id]!!.contains(stand))
                ticking[id]!!.add(stand)
        else
            ticking[id] = mutableListOf(stand)
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
            delay(20.ticks)

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

            StandAPI.log(ticking.toString())

            if (!ticking.containsKey(-1))
                continue

            val listIt = ticking[-1]!!.iterator()
            while (listIt.hasNext()){
                val stand = listIt.next()
                if (stand.getAttached() == null){
                    listIt.remove()
                    addWithId(stand, -2)
                    continue
                }
                val pE = Bukkit.getEntity(stand.getAttached()!!.first) ?: continue
                listIt.remove()
                addWithId(stand, pE.entityId)
                stand.setLocation(Location(
                    pE.world,
                    pE.location.x + stand.getAttached()!!.second.x,
                    pE.location.x + stand.getAttached()!!.second.y,
                    pE.location.x + stand.getAttached()!!.second.z
                ))
            }
        }
    }

}
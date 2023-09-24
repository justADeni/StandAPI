package com.github.justadeni.standapi

import com.github.justadeni.standapi.Misc.sendTo
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

object Ranger {

    private val ticking = hashMapOf<Int, MutableList<PacketStand>>()

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

    internal fun removeDetached(stand: PacketStand){
        ticking[-1]!!.remove(stand)
    }

    internal suspend fun tick() = withContext(StandAPI.getPlugin().asyncDispatcher){
        while (true) {

            val snapshotMap = ticking.toMap()

            for (world in snapshotMap.keys){
                for (stand in snapshotMap[world]!!){

                    val possiblePlayers = world.players.toMutableList().also { it.removeAll(stand.excludedPlayers()) }
                    val eligiblePlayers = stand.eligiblePlayers()
                    val includedPlayers = stand.includedPlayers

                    for (player in possiblePlayers){

                        val wasInside = includedPlayers.contains(player)
                        val isInside = eligiblePlayers.contains(player)

                        //Case 1: Player was inside detection range but went outside
                        if (wasInside && !isInside){
                            stand.destroyPacket.sendTo(listOf(player))
                            includedPlayers.remove(player)
                            break
                        }

                        //Case 2: Player was outside detection range but went inside
                        if (!wasInside && isInside){
                            stand.packetBundle.sendTo(listOf(player))
                            includedPlayers.add(player)
                            break
                        }

                        //Cases 3: and 4: Player is where they were, and we don't send anything
                    }
                }
            }

            delay(20.ticks)
        }
    }

}
package com.github.justadeni.standapi

import com.github.justadeni.standapi.Misc.sendTo
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player

object Ranger {

    private val ticking = hashMapOf<World, MutableList<PacketStand>>()

    fun add(stand: PacketStand){
        val w = stand.getLocation().world!!

        if (!ticking.contains(w))
            ticking[w] = mutableListOf(stand)
        else
            ticking[w]?.add(stand)
    }

    fun remove(stand: PacketStand){
        val w = stand.getLocation().world!!
        ticking[w]?.remove(stand)
    }

    /*
    suspend fun tick(){
        withContext(StandAPI.getPlugin().asyncDispatcher) {
            while (true){
                val snapshotMap = ticking.toMap()
                for (world in snapshotMap.keys){
                    val stands = snapshotMap[world]!!
                    for (stand in stands){
                        val eligiblePlayers = stand.eligiblePlayers()
                        stand.packetBundle.sendTo(eligiblePlayers)
                        stand.destroyPacket.sendTo(world.players.toMutableList().also { it.removeAll(eligiblePlayers) })
                    }
                }

                delay(100.ticks) //5 seconds
            }
        }
    }
    */

    suspend fun tick() = withContext(StandAPI.getPlugin().asyncDispatcher){
        while (true) {

            val snapshotMap = ticking.toMap()
            for (world in snapshotMap.keys){

                val stands = snapshotMap[world]!!

                for (stand in stands){
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

            delay(100.ticks)
        }
    }
}
package com.github.justadeni.standapi

import com.github.justadeni.standapi.Misc.sendTo
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.bukkit.World

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

}
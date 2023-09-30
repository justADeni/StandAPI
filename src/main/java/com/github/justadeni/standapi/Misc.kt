package com.github.justadeni.standapi

import com.comphenix.protocol.events.PacketContainer
import com.github.justadeni.standapi.datatype.Offset
import com.github.shynixn.mccoroutine.bukkit.launch
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.SplittableRandom
import kotlin.math.round

/**
 * @suppress
 */
object Misc {

    private var id = 9999

    private val manager = StandAPI.manager()

    internal fun currentID() = id

    private val rand = SplittableRandom()

    internal fun resetId(){
        val stands = Ranger.getAllStands()
        if (stands.isEmpty())
            return

        id = stands.sortedByDescending { it.id }[0].id
    }

    internal fun getID(): Int {
        id += 1
        return id
    }

    internal fun PacketContainer.sendTo(player: Player) = StandAPI.plugin().launch {
        manager.sendServerPacket(player, this@sendTo)
    }

    internal fun PacketContainer.sendTo(players: List<Player>) = StandAPI.plugin().launch {
        for (player in players){
            manager.sendServerPacket(player, this@sendTo)
        }
    }

    internal fun HashMap<Int, PacketContainer>.sendTo(player: Player) = StandAPI.plugin().launch {
        this@sendTo.values.forEach { manager.sendServerPacket(player, it) }
    }

    internal fun HashMap<Int, PacketContainer>.sendTo(players: List<Player>) = StandAPI.plugin().launch {
        for (player in players){
            this@sendTo.values.forEach { manager.sendServerPacket(player, it) }
        }
    }

    internal fun Int.squared(): Int{
        return this*this
    }

    internal fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
    }

    internal fun Float.round(decimals: Int): Float {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return (round(this * multiplier) / multiplier).toFloat()
    }

    internal fun Location.applyOffset(offset: Offset?): Location{
        if (offset == null)
            return this

        return Location(this.world, this.x + offset.x, this.y + offset.y, this.z + offset.z)
    }

    internal fun random10pc(): Boolean {
        return rand.nextInt() == 0
    }
}
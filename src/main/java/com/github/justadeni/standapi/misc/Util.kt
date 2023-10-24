package com.github.justadeni.standapi.misc

import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot
import com.github.justadeni.standapi.StandAPI
import com.github.justadeni.standapi.StandManager
import com.github.justadeni.standapi.datatype.Offset
import com.github.shynixn.mccoroutine.bukkit.launch
import kotlinx.coroutines.future.await
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.spigotmc.SpigotWorldConfig
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.round

/**
 * @suppress
 */
object Util {

    private var id = 47_483_647

    private val manager = StandAPI.manager()

    internal fun currentID() = id

    private val pTrackingRanges = hashMapOf<UUID, Int>()

    internal fun getPTrackingRange2(worlduuid: UUID): Int {
        return pTrackingRanges[worlduuid] ?: 2304 //48
    }

    internal fun initilazeTrackingRanges(){
        for (world in Bukkit.getWorlds()){
            pTrackingRanges[world.uid] = SpigotWorldConfig(world.name).playerTrackingRange.squared()
        }
    }

    internal suspend fun resetId(){
        val stands = StandManager.allAsync().await()
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

    internal fun Int.squared(): Int {
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

    internal fun Location.applyOffset(offset: Offset?): Location {
        if (offset == null)
            return this

        return Location(this.world, this.x + offset.x, this.y + offset.y, this.z + offset.z)
    }

    internal fun Player.isAnyoneNearby(): Boolean {
        return this@isAnyoneNearby.world.players
            .filterNot { it == this@isAnyoneNearby }
            .any { it.location.distanceSquared(this@isAnyoneNearby.location) < getPTrackingRange2(this.world.uid) }
    }

    internal fun getPlayerById(id: Int, world: World): Player? {
        return world.players.firstOrNull { it.entityId == id }
    }

    //now of course I could do this more elegantly with .name and Strings in one line, but switch is more performant and consumes no memory
    internal fun EWtoEQ(itemSlot: ItemSlot): EquipmentSlot {
        return when(itemSlot){
            ItemSlot.MAINHAND -> EquipmentSlot.HAND
            ItemSlot.OFFHAND -> EquipmentSlot.OFF_HAND
            ItemSlot.FEET -> EquipmentSlot.FEET
            ItemSlot.LEGS -> EquipmentSlot.LEGS
            ItemSlot.CHEST -> EquipmentSlot.CHEST
            ItemSlot.HEAD -> EquipmentSlot.HEAD
        }
    }

    internal fun EQtoEW(equipmentSlot: EquipmentSlot): ItemSlot {
        return when(equipmentSlot){
            EquipmentSlot.HAND -> ItemSlot.MAINHAND
            EquipmentSlot.OFF_HAND -> ItemSlot.OFFHAND
            EquipmentSlot.FEET -> ItemSlot.FEET
            EquipmentSlot.LEGS -> ItemSlot.LEGS
            EquipmentSlot.CHEST -> ItemSlot.CHEST
            EquipmentSlot.HEAD -> ItemSlot.HEAD
        }
    }
}
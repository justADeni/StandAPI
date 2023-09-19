package com.github.justadeni.standapi

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedDataValue
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject
import com.github.justadeni.standapi.storage.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.core.Rotations
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import java.util.*
import kotlin.collections.HashMap

//@Serializable
class PacketGenerator(private val id: Int,@Serializable(with = UUIDSerializer::class) private val uuid: UUID){

    @Transient
    private val watcher = WrappedDataWatcher()

    fun create(location: Location): PacketContainer{
        val packet = PacketContainer(PacketType.Play.Server.SPAWN_ENTITY)

        packet.integers.write(0,id)
        packet.uuiDs.write(0,uuid)
        packet.entityTypeModifier.write(0, EntityType.ARMOR_STAND)

        packet.doubles.write(0,location.x)
        packet.doubles.write(1,location.y)
        packet.doubles.write(2,location.z)

        return packet
    }

    fun destroy(): PacketContainer{
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_DESTROY)
        packet.intLists.write(0, listOf(id))

        return packet
    }

    fun equipment(map: HashMap<ItemSlot, ItemStack>): PacketContainer{
        val list = mutableListOf<com.comphenix.protocol.wrappers.Pair<ItemSlot, ItemStack>>()
        map.forEach { (t, u) ->  list.add(com.comphenix.protocol.wrappers.Pair(t, u))}
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT)

        packet.integers.write(0, id)
        packet.slotStackPairLists.write(0, list)

        return packet
    }

    fun metadata(bytes: Pair<Byte, Byte>, isCustomNameVisible: Boolean, customName: String, rotations: List<Rotations>): PacketContainer{
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_METADATA)
        packet.integers.write(0, id)
        val dataWatcher = watcher.deepClone()

        dataWatcher.setObject(WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(java.lang.Byte::class.java)), bytes.first)
        dataWatcher.setObject(WrappedDataWatcherObject(15, WrappedDataWatcher.Registry.get(java.lang.Byte::class.java)), bytes.second)
        dataWatcher.setObject(WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(java.lang.Boolean::class.java)), isCustomNameVisible)
        dataWatcher.setObject(WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true)), Optional.of(WrappedChatComponent.fromChatMessage(customName)[0].handle))

        for (index in 16..21){
            dataWatcher.setObject(WrappedDataWatcherObject(index, WrappedDataWatcher.Registry.get(Rotations::class.java)), rotations[index-16])
        }

        val wrappedDataValueList: MutableList<WrappedDataValue> = ArrayList()

        for (entry in dataWatcher.watchableObjects) {
            if (entry == null) continue
            val watcherObject = entry.watcherObject
            wrappedDataValueList.add(WrappedDataValue(watcherObject.index, watcherObject.serializer, entry.rawValue))
        }

        packet.dataValueCollectionModifier.write(0, wrappedDataValueList)

        return packet
    }

    fun teleport(newLocation: Location): PacketContainer{
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT)
        packet.integers.write(0, id)
        packet.doubles.write(0, newLocation.x)
        packet.doubles.write(1, newLocation.y)
        packet.doubles.write(2, newLocation.z)

        return packet
    }

    fun move(oldLocation: Location, newLocation: Location): PacketContainer{
        val packet = PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE)
        packet.integers.write(0, id)
        packet.shorts.write(0, ((newLocation.x * 32 - oldLocation.x * 32)*128).toInt().toShort())
        packet.shorts.write(1, ((newLocation.y * 32 - oldLocation.y * 32)*128).toInt().toShort())
        packet.shorts.write(2, ((newLocation.z * 32 - oldLocation.z * 32)*128).toInt().toShort())

        return packet
    }
}
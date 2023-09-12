package com.github.justadeni.standapi

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedDataValue
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject
import net.minecraft.core.Rotations
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.*


class PacketGenerator(val id: Int, val uuid: UUID){

    private val watcher = WrappedDataWatcher()

    fun create(location: Location): PacketContainer{
        val packet = PacketContainer(PacketType.Play.Server.SPAWN_ENTITY)

        packet.integers.write(0,id)
        packet.uuiDs.write(0,uuid)
        packet.entityTypeModifier.write(0, EntityType.ARMOR_STAND)

        //New Location
        packet.doubles.write(0,location.x)
        packet.doubles.write(1,location.y)
        packet.doubles.write(2,location.z)

        //Set velocity, optional
        //packet.integers.write(1, 0)
        //packet.integers.write(2, 0)
        //packet.integers.write(3, 0)

        return packet
    }

    fun destroy(): PacketContainer{
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_DESTROY)
        packet.intLists.write(0, listOf(id))

        return packet
    }

    fun equipment(list: MutableList<com.comphenix.protocol.wrappers.Pair<ItemSlot, ItemStack>>): PacketContainer{
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT)

        packet.integers.write(0, id)
        //val list: MutableList<com.comphenix.protocol.wrappers.Pair<ItemSlot, ItemStack>> = ArrayList()
        //list.add(com.comphenix.protocol.wrappers.Pair(slot, item))
        packet.slotStackPairLists.write(0, list)

        return packet
    }

    /*
    //just in case function
    fun headrotation(yaw: Float): PacketContainer {
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION)
        packet.modifier.writeDefaults()
        packet.integers.write(0, id)
        packet.bytes.write(0, (yaw*256f / 360f).toInt().toByte())
        return packet
    }
    */

    /*
    fun metadata(index: Int, byte: Byte): PacketContainer {
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_METADATA)
        packet.integers.write(0, id);

        val byteSerializer = WrappedDataWatcher.Registry.get(java.lang.Byte::class.java)

        val entityBitmask = WrappedDataWatcherObject(0, byteSerializer)
        val skinData = WrappedDataWatcherObject(15, byteSerializer)

        val watcher = WrappedDataWatcher()

        watcher.setObject(entityBitmask, 0x20.toByte())
        watcher.setObject(skinData, (0x01 and 0x08).toByte())

        packet.watchableCollectionModifier.write(0, watcher.watchableObjects);

        return packet
    }
    */

    /*
    fun metadata(properties: Map<Int, Any>): PacketContainer{
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_METADATA)
        packet.integers.write(0, id)
        val dataWatcher = watcher.deepClone()
        packet.watchableCollectionModifier.write(0, dataWatcher.watchableObjects)

        for (index in properties.keys) {
            when (val info = properties[index]) {
                is String -> {
                    val chatSerializer: WrappedDataWatcher.Serializer =
                        WrappedDataWatcher.Registry.getChatComponentSerializer(true)
                    val optChatFieldWatcher = WrappedDataWatcherObject(index, chatSerializer)
                    val optChatField = Optional.of(WrappedChatComponent.fromChatMessage(info)[0].handle)
                    dataWatcher.setObject(optChatFieldWatcher, optChatField)
                }

                is Byte -> {
                    val setByte = WrappedDataWatcherObject(index, WrappedDataWatcher.Registry.get(Byte::class.java))
                    dataWatcher.setObject(setByte, info.toByte())
                    //dataWatcher.setObject(index, info)
                }
            }
        }

        val wrappedDataValueList: MutableList<WrappedDataValue> = Lists.newArrayList()
        dataWatcher.watchableObjects.stream()
            .filter(Objects::nonNull)
            .forEach { entry ->
            val dataWatcherObject: WrappedDataWatcherObject = entry.watcherObject
            wrappedDataValueList.add(WrappedDataValue(dataWatcherObject.index, dataWatcherObject.serializer, entry.rawValue))
        }
        packet.dataValueCollectionModifier.write(0, wrappedDataValueList)

        return packet
    }
    */

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
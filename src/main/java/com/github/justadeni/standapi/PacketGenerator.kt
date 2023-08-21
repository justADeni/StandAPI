package com.github.justadeni.standapi

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemStack
import org.bukkit.util.EulerAngle
import org.joml.Vector3f
import java.util.*

class PacketGenerator(val id: Int, val uuid: UUID){

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
        packet.integers.write(1, 0)
        packet.integers.write(2, 0)
        packet.integers.write(3, 0)

        // Set yaw pitch
        packet.integers.write(4, 0)
        packet.integers.write(5, 0)

        // Set object data
        packet.integers.write(7, 0)

        return packet
    }

    fun destroy(): PacketContainer{
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_DESTROY)

        packet.integers.write(0, 1)
        packet.integerArrays.write(0,intArrayOf(id))

        return packet
    }

    fun equipment(itemStack: ItemStack): PacketContainer{
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT)

        packet.integers.write(0, id)
        packet.itemSlots.write(0, EnumWrappers.ItemSlot.HEAD)
        packet.itemModifier.write(0, itemStack)

        return packet
    }

    fun headrotation(eulerAngle: EulerAngle): PacketContainer{
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_METADATA)

        packet.modifier.writeDefaults()
        packet.integers.write(0, id)
        val dataWatcher = WrappedDataWatcher(packet.watchableCollectionModifier.read(0))
        val serializer = WrappedDataWatcher.Registry.get(Vector3f::class.java)
        /*
        val eulerAngle = EulerAngle(
            Math.toRadians(yaw.toDouble()),
            Math.toRadians(pitch.toDouble()),
            Math.toRadians(roll.toDouble())
        )
        */
        val vector3f = Vector3f(
            Math.toDegrees(eulerAngle.x).toFloat(),
            Math.toDegrees(eulerAngle.y).toFloat(),
            Math.toDegrees(eulerAngle.z).toFloat()
        )

        dataWatcher.setObject(15, serializer, vector3f)
        dataWatcher.setObject(16, serializer, vector3f)
        dataWatcher.setObject(17, serializer, vector3f)
        dataWatcher.setObject(18, serializer, vector3f)
        dataWatcher.setObject(19, serializer, vector3f)
        dataWatcher.setObject(20, serializer, vector3f)

        packet.watchableCollectionModifier.write(0, dataWatcher.watchableObjects)

        return packet
    }

    //just in case function
    fun setLook(yaw: Float): PacketContainer {
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_HEAD_ROTATION)
        packet.modifier.writeDefaults()
        packet.integers.write(0, id)
        packet.bytes.write(0, (yaw*256f / 360f).toInt().toByte())
        return packet
    }
    /*
    fun location(id: Int, location: Location): PacketContainer{
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT)

        packet.integers.write(0,id)

        //New Location
        packet.doubles.write(0,location.x)
        packet.doubles.write(1,location.y)
        packet.doubles.write(2,location.z)

        //Yaw and Pitch
        packet.integers.write(1,0)
        packet.integers.write(2,0)

        //On Ground
        packet.booleans.write(0,false)

        return packet
    }
    */
}
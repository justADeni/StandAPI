package com.github.justadeni.standapi

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import com.github.justadeni.standapi.Misc.sendTo
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.EulerAngle
import org.joml.Vector3f
import java.util.*


class PacketStand(var location: Location) {

    val id = Misc.getID()
    val uuid = UUID.randomUUID()
    val players: HashSet<Player> = HashSet()

    private fun birth(deltaP: List<Player>){
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

        packet.sendTo(deltaP)
    }

    private fun death(deltaP: List<Player>){
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_DESTROY)

        packet.integers.write(0, 1)
        packet.integerArrays.write(0,intArrayOf(id))

        packet.sendTo(deltaP)
    }

    fun headEquipment(item: ItemStack){
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT)

        packet.integers.write(0, id)
        packet.itemSlots.write(0, EnumWrappers.ItemSlot.HEAD)
        packet.itemModifier.write(0, item)

        packet.sendTo(players.toList())
    }

    //TODO: Rework this mess
    fun headRotation(yaw: Float, pitch: Float, roll: Float){
        val packet = PacketContainer(PacketType.Play.Server.ENTITY_METADATA)

        packet.modifier.writeDefaults()
        packet.integers.write(0, id)
        val dataWatcher = WrappedDataWatcher(packet.watchableCollectionModifier.read(0))
        val serializer = WrappedDataWatcher.Registry.get(Vector3f::class.java)

        val eulerAngle = EulerAngle(
            Math.toRadians(yaw.toDouble()),
            Math.toRadians(pitch.toDouble()),
            Math.toRadians(roll.toDouble())
        )
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

        packet.sendTo(players.toList())
    }

    fun teleport(deltaL: Location){
        location = deltaL
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

        packet.sendTo(players.toList())
    }

    fun addSeeing(deltaP: List<Player>){
        birth(deltaP)
        players.addAll(deltaP)
    }

    fun removeSeeing(deltaP: List<Player>){
        death(deltaP)
        players.removeAll(deltaP.toSet())
    }
}
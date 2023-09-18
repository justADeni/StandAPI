package com.github.justadeni.standapi.storage

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.UUID

class LocationSerializer(override val descriptor: SerialDescriptor) : KSerializer<Location> {
    override fun serialize(encoder: Encoder, value: Location) {
        encoder.encodeString("${value.world!!.uid},${value.x},${value.y},${value.z}")
    }

    override fun deserialize(decoder: Decoder): Location {
        val parts = decoder.decodeString().split(",")
        val world = Bukkit.getWorld(UUID.fromString(parts[0]))
        val x = parts[1].toDouble()
        val y = parts[2].toDouble()
        val z = parts[3].toDouble()
        return Location(world, x, y, z)
    }
}
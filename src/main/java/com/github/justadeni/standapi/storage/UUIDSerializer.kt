package com.github.justadeni.standapi.storage

import com.github.justadeni.standapi.PacketStand
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

class UUIDSerializer(): KSerializer<UUID> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("com.github.justadeni.standapi.storage.UUIDSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

}
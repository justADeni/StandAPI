package com.github.justadeni.standapi.serialization

import com.github.justadeni.standapi.datatype.Offset
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class OffsetSerializer: KSerializer<Offset> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("com.github.justadeni.standapi.serialization.OffsetSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Offset) {
        encoder.encodeString("${value.x},${value.y},${value.z}")
    }

    override fun deserialize(decoder: Decoder): Offset {
        val parts = decoder.decodeString().split(",")
        return Offset(parts[0].toDouble(),parts[1].toDouble(),parts[2].toDouble())
    }
}
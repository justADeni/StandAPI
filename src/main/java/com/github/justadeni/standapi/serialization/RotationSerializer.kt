package com.github.justadeni.standapi.serialization

import com.github.justadeni.standapi.datatype.Rotation
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class RotationSerializer(): KSerializer<Rotation> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("com.github.justadeni.standapi.storage.RotationSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Rotation) {
        encoder.encodeString("${value.pitch},${value.yaw},${value.roll}")
    }

    override fun deserialize(decoder: Decoder): Rotation {
        val parts = decoder.decodeString().split(",")
        return Rotation(parts[0].toFloat(), parts[1].toFloat(), parts[2].toFloat())
    }

}
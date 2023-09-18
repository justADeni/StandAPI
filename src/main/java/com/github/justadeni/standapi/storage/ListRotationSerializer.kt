package com.github.justadeni.standapi.storage

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class ListRotationSerializer(override val descriptor: SerialDescriptor) : KSerializer<MutableList<Rotation>> {

    //TODO: Finish this aswell

    override fun serialize(encoder: Encoder, value: MutableList<Rotation>) {
        var s = ""
        value.forEach { s = s.plus(RotationSerializer(descriptor).serialize(encoder, it) + "|") }
        encoder.encodeString(s)
    }

    override fun deserialize(decoder: Decoder): MutableList<Rotation> {

    }
}
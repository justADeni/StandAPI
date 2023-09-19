package com.github.justadeni.standapi.storage

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class ListRotationSerializer(override val descriptor: SerialDescriptor) : KSerializer<MutableList<Rotation>> {

    override fun serialize(encoder: Encoder, value: MutableList<Rotation>) {
        var string = ""
        value.forEach { string += "${it.pitch},${it.yaw},${it.roll}|" }
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): MutableList<Rotation> {
        val list = mutableListOf<Rotation>()
        val listParts = decoder.decodeString().split("|")
        for (part in listParts){
            if (!part.contains(","))
                continue

            val rotationParts = part.split(",")
            list.add(Rotation(rotationParts[0].toFloat(), rotationParts[1].toFloat(), rotationParts[2].toFloat()))
        }

        return list
    }
}
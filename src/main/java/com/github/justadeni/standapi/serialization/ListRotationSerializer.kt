package com.github.justadeni.standapi.serialization

import com.github.justadeni.standapi.misc.Util.round
import com.github.justadeni.standapi.datatype.Rotation
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * @suppress
 */
class ListRotationSerializer() : KSerializer<MutableList<Rotation>> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("com.github.justadeni.standapi.serialization.ListRotationSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: MutableList<Rotation>) {
        var string = ""
        value.forEach { string += "${it.pitch.round(3)},${it.yaw.round(3)},${it.roll.round(3)}|" }
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): MutableList<Rotation> {
        val list = mutableListOf<Rotation>()
        val listParts = decoder.decodeString().split("|")
        for (part in listParts){
            if (part.isEmpty() || !part.contains(","))
                continue

            val rotationParts = part.split(",")
            list.add(Rotation(rotationParts[0].toFloat(), rotationParts[1].toFloat(), rotationParts[2].toFloat()))
        }

        return list
    }
}
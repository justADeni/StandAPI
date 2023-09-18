package com.github.justadeni.standapi.storage

import com.comphenix.protocol.wrappers.EnumWrappers
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = EnumWrappers.ItemSlot::class)
class EnumISSerializer(override val descriptor: SerialDescriptor) {

    override fun serialize(encoder: Encoder, value: EnumWrappers.ItemSlot) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): EnumWrappers.ItemSlot {
        return EnumWrappers.ItemSlot.valueOf(decoder.decodeString())
    }

}
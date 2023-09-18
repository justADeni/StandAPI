package com.github.justadeni.standapi.storage

import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot
import com.google.common.io.BaseEncoding.base64
import it.unimi.dsi.fastutil.Hash
import kotlinx.serialization.Contextual
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.internal.MapLikeSerializer
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class PairSerializer(override val descriptor: SerialDescriptor) : KSerializer<HashMap<ItemSlot, ItemStack>> {

//TODO: rewrite dis
    override fun serialize(encoder: Encoder, value: HashMap<ItemSlot, ItemStack>) {
        TODO("Not yet implemented")
    }

    override fun serialize(encoder: Encoder, value: Pair<ItemSlot, ItemStack>) {
        val first = value.first.name

        val outputStream = ByteArrayOutputStream()
        val dataOutput = BukkitObjectOutputStream(outputStream)
        dataOutput.writeObject(value.second)
        dataOutput.close()

        val second = Base64Coder.encodeLines(outputStream.toByteArray())

        encoder.encodeString("$first|$second")
    }

    override fun deserialize(decoder: Decoder): MutableMap<ItemSlot, ItemStack> {
        val pair = decoder.decodeString().split("|", limit = 2)
        val first = EnumWrappers.ItemSlot.valueOf(pair[0])

        val inputStream = ByteArrayInputStream(Base64Coder.decodeLines(pair[1]))
        val dataInput = BukkitObjectInputStream(inputStream)

        val second = dataInput.readObject() as ItemStack
        dataInput.close()

        return Pair(first, second)
    }
}
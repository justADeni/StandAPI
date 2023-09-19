package com.github.justadeni.standapi.storage

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class ItemStackSerializer() : KSerializer<ItemStack> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("com.github.justadeni.standapi.storage.ItemStackSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ItemStack) {
        val outputStream = ByteArrayOutputStream()
        val dataOutput = BukkitObjectOutputStream(outputStream)
        dataOutput.writeObject(value)
        dataOutput.close()

        val item = Base64Coder.encodeLines(outputStream.toByteArray())

        encoder.encodeString(item)
    }

    override fun deserialize(decoder: Decoder): ItemStack {
        val inputStream = ByteArrayInputStream(Base64Coder.decodeLines(decoder.decodeString()))
        val dataInput = BukkitObjectInputStream(inputStream)

        val item = dataInput.readObject() as ItemStack
        dataInput.close()
        return item
    }
}
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

    override fun serialize(encoder: Encoder, value: HashMap<ItemSlot, ItemStack>) {
        var string = ""
        value.forEach { (t, u) ->
            string += t.name + ","

            val outputStream = ByteArrayOutputStream()
            val dataOutput = BukkitObjectOutputStream(outputStream)
            dataOutput.writeObject(u)
            dataOutput.close()

            string += Base64Coder.encodeLines(outputStream.toByteArray()) + "|"
        }
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): HashMap<ItemSlot, ItemStack> {

        val map = hashMapOf<ItemSlot, ItemStack>()

        val mapParts = decoder.decodeString().split("|")
        for (part in mapParts){
            if (!part.contains(","))
                continue

            val pairParts = part.split(",")

            val slot = ItemSlot.valueOf(pairParts[0])

            val inputStream = ByteArrayInputStream(Base64Coder.decodeLines(pairParts[1]))
            val dataInput = BukkitObjectInputStream(inputStream)

            val item = dataInput.readObject() as ItemStack

            map[slot] = item
        }
        return map
    }
}
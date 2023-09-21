package com.github.justadeni.standapi.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack

class ItemStackSerializer() : KSerializer<ItemStack> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("com.github.justadeni.standapi.serialization.ItemStackSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ItemStack) {
        val itemConfig = YamlConfiguration()
        itemConfig.set("item", value)
        val serialized: String = itemConfig.saveToString()
        encoder.encodeString(serialized)
    }

    override fun deserialize(decoder: Decoder): ItemStack {
        val restoreConfig = YamlConfiguration()
        restoreConfig.loadFromString(decoder.decodeString())
        return restoreConfig.getItemStack("item")!!
    }
}
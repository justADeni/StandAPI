package com.github.justadeni.standapi.serialization

import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack

class PairSerializer() : KSerializer<HashMap<ItemSlot, ItemStack>> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("com.github.justadeni.standapi.storage.PairSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: HashMap<ItemSlot, ItemStack>) {
        val itemConfig = YamlConfiguration()
        value.forEach { (t, u) ->
            itemConfig.set(t.name, u)
        }
        val serialized: String = itemConfig.saveToString()
        encoder.encodeString(serialized)
    }

    override fun deserialize(decoder: Decoder): HashMap<ItemSlot, ItemStack> {
        val map = hashMapOf<ItemSlot, ItemStack>()
        val restoreConfig = YamlConfiguration()
        restoreConfig.loadFromString(decoder.decodeString())
        restoreConfig.getKeys(false).forEach {
            map[ItemSlot.valueOf(it)] = restoreConfig.getItemStack(it)!!
        }
        return map
    }
}
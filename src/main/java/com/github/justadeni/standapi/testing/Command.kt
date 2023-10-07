package com.github.justadeni.standapi.testing

import com.comphenix.protocol.wrappers.EnumWrappers
import com.github.justadeni.standapi.PacketStand
import com.github.justadeni.standapi.StandAPI
import com.github.justadeni.standapi.datatype.Offset
import com.github.justadeni.standapi.datatype.Rotation
import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * @suppress
 */
class Command: SuspendingCommandExecutor {

    companion object {
        private var testStand: PacketStand? = null
        private var serializedString: String = ""
    }

    override suspend fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        if (sender !is Player)
            return true

        if (!sender.hasPermission("standapi.admin"))
            return true

        when (args[0].lowercase()){
            "spawn" -> {
                sender.sendMessage("stand spawned!")
                testStand = PacketStand(sender.location, "StandAPI_test")
            }
            "equipment" -> {
                sender.sendMessage("stand equipped")
                testStand!!.setEquipment(EnumWrappers.ItemSlot.HEAD, ItemStack(Material.ACACIA_CHEST_BOAT))
            }
            "metadata" -> {
                sender.sendMessage("stand metadata sent")
                //testStand!!.setGlowingEffect(true)
                //testStand!!.setArms(true)
                testStand!!.setSmall(true)
                testStand!!.setNoBaseplate(true)
                testStand!!.setCustomNameVisible(true)
                testStand!!.setCustomName("test name")
            }
            "invisible" -> {
                sender.sendMessage("stand made invisible")
                testStand!!.setInvisible(true)
                testStand!!.setMarker(true)
            }
            "location" -> {
                sender.sendMessage("stand teleported")
                testStand!!.setLocation(sender.location)
            }
            "destroy" -> {
                sender.sendMessage("stand destroyed")
                testStand!!.remove()
                testStand = null
            }
            "rotatehead" -> {
                sender.sendMessage("stand head rotating")
                StandAPI.plugin().launch {
                    for (i in 0..360){
                        testStand!!.setHeadPose(Rotation(i.toFloat(), 0f, 0f))
                        delay(1.ticks)
                    }
                }
            }
            "serialize" -> {
                sender.sendMessage("stand serializing")
                serializedString = Json.encodeToString(testStand!!)
                Bukkit.broadcastMessage(serializedString)
                testStand!!.remove()
                testStand = null
            }
            "deserialize" -> {
                sender.sendMessage("stand deserializing")
                testStand = Json.decodeFromString(serializedString)
                serializedString = ""
            }
            "attach" -> {
                sender.sendMessage("stand attached")
                testStand!!.attachTo(sender, Offset(0.5,3.0,0.5))
            }
            "detach" -> {
                sender.sendMessage("stand detached")
                testStand!!.detachFrom()
            }
        }

        return false
    }

}
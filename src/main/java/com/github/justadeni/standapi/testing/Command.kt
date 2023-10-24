package com.github.justadeni.standapi.testing

import com.github.justadeni.standapi.PacketStand
import com.github.justadeni.standapi.PacketStand.Companion.fromRealStand
import com.github.justadeni.standapi.StandAPI
import com.github.justadeni.standapi.datatype.Offset
import com.github.justadeni.standapi.datatype.Rotation
import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.zorbeytorunoglu.kLib.cuboid.Cuboid
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

/**
 * @suppress
 */
class Command: SuspendingCommandExecutor {

    companion object {
        private var testStand: PacketStand? = null
        private var realStand: ArmorStand? = null
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
                testStand!!.setEquipment(EquipmentSlot.HEAD, ItemStack(Material.ACACIA_CHEST_BOAT))
            }
            "metadata" -> {
                sender.sendMessage("stand metadata sent")
                //testStand!!.setGlowingEffect(true)
                //testStand!!.setArms(true)
                testStand!!.setSmall(true)
                testStand!!.setBaseplate(false)
                testStand!!.setCustomNameVisible(true)
                testStand!!.setCustomName("test name")
            }
            "invisible" -> {
                sender.sendMessage("stand made invisible")
                testStand!!.setVisible(false)
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
            "headpitch" -> {
                sender.sendMessage("stand head rotating")
                StandAPI.plugin().launch {
                    for (i in 0..360){
                        testStand!!.setHeadPose(Rotation(i.toFloat(), 0f, 0f))
                        delay(1.ticks)
                    }
                }
            }
            "headyaw" -> {
                sender.sendMessage("stand head rotating")
                StandAPI.plugin().launch {
                    for (i in 0..360){
                        testStand!!.setHeadPose(Rotation(0f, i.toFloat(), 0f))
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
            "torealstand" -> {
                sender.sendMessage("real stand created")
                realStand = testStand!!.toRealStand()
                testStand = null
            }
            "fromrealstand" -> {
                sender.sendMessage("packet stand created")
                testStand = realStand!!.fromRealStand()
                realStand = null
            }
            "stresstest" -> {
                val cuboid = Cuboid(sender.location.add(-100.0,-100.0,-100.0), sender.location.add(100.0,100.0,100.0))
                for (i in 0..100_000){
                    delay(5)
                    StandAPI.plugin().launch() {
                        PacketStand(cuboid.randomLocation)
                    }
                }
            }
        }

        return false
    }

}
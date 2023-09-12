package com.github.justadeni.standapi.testing

import com.comphenix.protocol.wrappers.EnumWrappers
import com.github.justadeni.standapi.PacketStand
import com.github.justadeni.standapi.StandAPI
import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.delay
import net.minecraft.core.Rotations
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class TestCommand: SuspendingCommandExecutor {

    companion object {
        var testStand: PacketStand? = null
    }

    override suspend fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        if (sender !is Player)
            return false

        when (args[0]){
            "spawn" -> {
                sender.sendMessage("stand spawned!")
                testStand = PacketStand(sender.location)
                testStand!!.setRenderDistance(1)
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
            }
            "rotatehead" -> {
                sender.sendMessage("stand head rotating")
                StandAPI.getPlugin().launch {
                    for (i in 0..360){
                        testStand!!.setHeadPose(Rotations(i.toFloat(), 0f, 0f))
                        delay(5.ticks)
                    }
                }
            }
        }

        return false
    }

}
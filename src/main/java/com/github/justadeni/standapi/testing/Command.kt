package com.github.justadeni.standapi.testing

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Command: CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {

        /*
        if (args.size > 1)
            return true

        if (args.size < 1)
            return true
        */

        if (args[0].equals("spawnStatic", ignoreCase = true)) {
            sender.sendMessage("stand spawned!")
            Tests.spawnStatic(sender as Player)
        }

        return false
    }

}
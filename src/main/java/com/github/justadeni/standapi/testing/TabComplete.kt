package com.github.justadeni.standapi.testing

import com.github.shynixn.mccoroutine.bukkit.SuspendingTabCompleter
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

/**
 * @suppress
 */
class TabComplete: SuspendingTabCompleter {

    private val fullList = listOf("spawn", "equipment", "metadata", "invisible", "location", "destroy", "headpitch", "headyaw", "serialize", "deserialize", "attach", "detach", "torealstand", "fromrealstand")

    override suspend fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {

        if (!sender.hasPermission("standapi.admin"))
            return emptyList()

        if (args.size > 1)
            return emptyList()

        return fullList.filter { it.contains(args[0].lowercase()) }
    }
}
package com.github.justadeni.standapi.testing

import com.github.justadeni.standapi.storage.StandApiConfig
import com.github.shynixn.mccoroutine.bukkit.SuspendingTabCompleter
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

/**
 * @suppress
 */
class TabComplete: SuspendingTabCompleter {

    val fullList = listOf("reload", "spawn", "equipment", "metadata", "invisible", "location", "destroy", "rotatehead", "serialize", "deserialize", "attach", "detach")

    override suspend fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {

        if (!sender.hasPermission("standapi.admin"))
            return emptyList()

        if (args.size > 1)
            return emptyList()

        if (!StandApiConfig.testMode)
            return if ("reload".contains(args[0].lowercase()))
                listOf("reload")
            else
                emptyList()

        return fullList.filter { it.contains(args[0].lowercase()) }
    }
}
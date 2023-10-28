package com.github.justadeni.standapi.misc

import com.github.justadeni.standapi.StandAPI
import org.bukkit.ChatColor

/**
 * @suppress
 */
object Logger {

    private val logger = StandAPI.plugin().logger

    internal fun warn(msg: String){
        logger.warning(ChatColor.translateAlternateColorCodes('&', msg))
    }

    internal fun log(msg: String){
        logger.info(ChatColor.translateAlternateColorCodes('&', msg))
    }
}
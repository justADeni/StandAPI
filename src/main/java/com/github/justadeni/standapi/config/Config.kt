package com.github.justadeni.standapi.config

import com.github.justadeni.standapi.Misc.squared
import com.github.justadeni.standapi.StandAPI
import org.bukkit.configuration.file.FileConfiguration

object Config {

    //TODO: write comments in config and map values to objects

    val config = StandAPI.getPlugin().config

    internal fun renderDistance2() = config.getInt("RenderDistance").squared()
    internal fun savingEnabled() = config.getBoolean("SavingEnabled")
    internal fun reload() = StandAPI.getPlugin().reloadConfig()
}
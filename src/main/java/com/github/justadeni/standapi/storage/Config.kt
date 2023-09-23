package com.github.justadeni.standapi.storage

import com.github.justadeni.standapi.Misc.squared
import com.github.justadeni.standapi.StandAPI

object Config {

    private val config = StandAPI.getPlugin().config

    internal var renderDistance2 = config.getInt("RenderDistance").squared()
    internal var savingEnabled = config.getBoolean("SavingEnabled")
    internal var testMode = config.getBoolean("TestMode")

    internal fun reload() {
        StandAPI.getPlugin().reloadConfig()
        renderDistance2 = config.getInt("RenderDistance").squared()
        savingEnabled = config.getBoolean("SavingEnabled")
        testMode = config.getBoolean("TestMode")
    }
}
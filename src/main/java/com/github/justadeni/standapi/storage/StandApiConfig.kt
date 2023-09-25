package com.github.justadeni.standapi.storage

import com.github.justadeni.standapi.Misc.squared
import com.github.justadeni.standapi.StandAPI

object StandApiConfig {

    private val config = StandAPI.plugin().config

    internal var renderDistance2 = config.getInt("RenderDistance").squared()
    internal var savingEnabled = config.getBoolean("SavingEnabled")
    internal var testMode = config.getBoolean("TestMode")

    internal fun reload() {
        StandAPI.plugin().reloadConfig()
        renderDistance2 = config.getInt("RenderDistance").squared()
        savingEnabled = config.getBoolean("SavingEnabled")
        testMode = config.getBoolean("TestMode")
    }

    fun setRenderDistance(rd: Int){
        config.set("RenderDistance", rd)
        StandAPI.plugin().saveConfig()
        renderDistance2 = rd.squared()
    }

    fun setSavingEnabled(se: Boolean){
        config.set("SavingEnabled", se)
        StandAPI.plugin().saveConfig()
        savingEnabled = se
    }

    fun testMode(tm: Boolean){
        config.set("TestMode", tm)
        StandAPI.plugin().saveConfig()
        testMode = tm
    }
}
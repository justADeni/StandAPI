package com.github.justadeni.standapi.storage

import com.github.justadeni.standapi.Misc.squared
import com.github.justadeni.standapi.StandAPI
import kotlin.math.sqrt

object StandApiConfig {

    private val config = StandAPI.plugin().config

    private var renderDistance2 = config.getInt("RenderDistance").squared()
    private var savingEnabled = config.getBoolean("SavingEnabled")
    private var testMode = config.getBoolean("TestMode")

    internal fun reload() {
        StandAPI.plugin().reloadConfig()
        renderDistance2 = config.getInt("RenderDistance").squared()
        savingEnabled = config.getBoolean("SavingEnabled")
        testMode = config.getBoolean("TestMode")
    }

    /**
     * returns render distance squared
     */
    @JvmStatic
    fun getRenderDistance2() = renderDistance2

    /**
     * sets render distance in blocks (not squared)
     */
    @JvmStatic
    fun setRenderDistance(rd: Int){
        config.set("RenderDistance", rd)
        StandAPI.plugin().saveConfig()
        renderDistance2 = rd.squared()
    }

    /**
     * returns if saving and loading is enabled
     */
    @JvmStatic
    fun getSavingEnabled() = savingEnabled

    /**
     * set saving to specified value
     */
    @JvmStatic
    fun setSavingEnabled(se: Boolean){
        config.set("SavingEnabled", se)
        StandAPI.plugin().saveConfig()
        savingEnabled = se
    }

    internal fun getTestMode() = testMode

    internal fun testMode(tm: Boolean){
        config.set("TestMode", tm)
        StandAPI.plugin().saveConfig()
        testMode = tm
    }
}
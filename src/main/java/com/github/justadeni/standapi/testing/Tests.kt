package com.github.justadeni.standapi.testing

import com.github.justadeni.standapi.stat.StaticStand
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.EulerAngle
import kotlin.random.Random

object Tests {

    private val r = Random(0)

    fun spawnStatic(p: Player){
        val stand = StaticStand(p.location, 77f, ItemStack(Material.STICK))
        stand.addSeeing(listOf(p))
    }

}
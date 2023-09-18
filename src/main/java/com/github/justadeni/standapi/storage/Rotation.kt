package com.github.justadeni.standapi.storage

import net.minecraft.core.Rotations

class Rotation(
    val pitch: Float = 0f,
    val yaw: Float = 0f,
    val roll: Float = 0f
): Rotations(pitch, yaw, roll) {}
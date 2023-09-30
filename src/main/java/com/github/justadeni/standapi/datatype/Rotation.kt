package com.github.justadeni.standapi.datatype

import net.minecraft.core.Rotations
import org.bukkit.util.EulerAngle

class Rotation(
    val pitch: Float = 0f,
    val yaw: Float = 0f,
    val roll: Float = 0f
): Rotations(pitch, yaw, roll) {

    companion object {

        @JvmStatic
        fun EulerAngle.toRotation(): Rotation {
            return Rotation(Math.toDegrees(this.x).toFloat(), Math.toDegrees(this.y).toFloat(), Math.toDegrees(this.z).toFloat())
        }

        @JvmStatic
        fun Rotation.toEulerAngle(): EulerAngle {
            return EulerAngle(Math.toRadians(this.x.toDouble()), Math.toRadians(this.y.toDouble()), Math.toRadians(this.z.toDouble()))
        }
    }
}
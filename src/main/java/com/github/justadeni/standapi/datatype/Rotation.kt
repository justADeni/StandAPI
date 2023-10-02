package com.github.justadeni.standapi.datatype

import net.minecraft.core.Rotations
import org.bukkit.util.EulerAngle

class Rotation(pitch: Float = 0f, yaw: Float = 0f, roll: Float = 0f): Rotations(pitch, yaw, roll) {

    val pitch = if (super.x < 0f) super.x + 360f else super.x
    val yaw = if (super.y < 0f) super.y + 360f else super.y
    val roll = if (super.z < 0f) super.z + 360f else super.z

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
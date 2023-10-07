package com.github.justadeni.standapi.datatype

import org.bukkit.util.EulerAngle
import net.minecraft.core.Vector3f

class Rotation(pitch: Float = 0f, yaw: Float = 0f, roll: Float = 0f): Vector3f(pitch, yaw, roll) {

    val pitch = if (super.a < 0f) super.a + 360f else super.a
    val yaw = if (super.b < 0f) super.b + 360f else super.b
    val roll = if (super.c < 0f) super.c + 360f else super.c

    companion object {

        @JvmStatic
        fun EulerAngle.toRotation(): Rotation {
            return Rotation(Math.toDegrees(this.x).toFloat(), Math.toDegrees(this.y).toFloat(), Math.toDegrees(this.z).toFloat())
        }

        @JvmStatic
        fun Rotation.toEulerAngle(): EulerAngle {
            return EulerAngle(Math.toRadians(this.pitch.toDouble()), Math.toRadians(this.yaw.toDouble()), Math.toRadians(this.roll.toDouble()))
        }
    }
}
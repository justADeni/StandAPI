package com.github.justadeni.standapi.datatype

import org.bukkit.util.EulerAngle
import net.minecraft.core.Vector3f

/**
 * represents pitch, yaw and roll of stand part in degrees 0-360f
 */
class Rotation(pitch: Float = 0f, yaw: Float = 0f, roll: Float = 0f): Vector3f(pitch, yaw, roll) {

    val pitch = thetikos(super.a)
    val yaw = thetikos(super.b)
    val roll = thetikos(super.c)

    private fun thetikos(float: Float) = if (float < 0f) float + 360f else float

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
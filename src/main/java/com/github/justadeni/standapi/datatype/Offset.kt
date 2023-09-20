package com.github.justadeni.standapi.datatype

data class Offset(val x: Double, val y: Double, val z: Double) {

    companion object {
        val ZERO = Offset(0.0,0.0,0.0)
    }
}
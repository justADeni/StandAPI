package com.github.justadeni.standapi.misc

/**
 * @suppress
 */
internal class Counter(private val from: Int, private val to: Int) {

    private var i = from;

    internal fun reached(): Boolean {
        i++
        if (i > to) {
            i = from
            return true
        }
        return false
    }

}
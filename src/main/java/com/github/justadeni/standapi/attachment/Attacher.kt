package com.github.justadeni.standapi.attachment

import com.github.justadeni.standapi.Ranger
import com.github.justadeni.standapi.datatype.Offset
import com.google.common.collect.HashMultimap

object Attacher {
    private val attachedMap = HashMultimap.create<Int, Pair<Int, Offset>>()

    internal fun getMap() = attachedMap

    internal fun add(id: Int, stand: Pair<Int, Offset>){
        attachedMap.put(id, stand)
    }

    internal fun removeValue(id: Int){
        val mapit = attachedMap.keys().iterator()
        while (mapit.hasNext()){
            val key = mapit.next()
            val pairit = attachedMap[key].iterator()
            while (pairit.hasNext()){
                val pair = pairit.next()

                if (pair.first != id)
                    continue

                attachedMap.remove(key, pair.first)
            }
        }
        Ranger.find(id)?.detachFrom()
    }

    internal fun removeKey(id: Int){
        for (value in attachedMap[id]){
            Ranger.find(value.first)?.detachFrom()
        }

        attachedMap.removeAll(id)
    }
}
package com.github.justadeni.standapi.attachment

import com.github.justadeni.standapi.PacketStand
import com.github.justadeni.standapi.datatype.Offset
import org.bukkit.entity.Entity

object Attacher {

    private val attached = HashMap<Int, MutableList<Pair<Int, Offset>>>()

    fun PacketStand.attachTo(entity: Entity) {
        this.attachTo(entity, Offset.ZERO)
    }

    fun PacketStand.attachTo(entity: Entity, offset: Offset) {
        val key = entity.entityId

        remove(this.id)

        if (!attached.containsKey(key)) {
            attached[key] = mutableListOf(Pair(this.id, offset))
            return
        }

        attached[key]?.add(Pair(this.id, offset))
    }

    fun PacketStand.detach(){
        remove(this.id)
    }

    internal fun getMap(): Map<Int, MutableList<Pair<Int, Offset>>> = attached

    internal fun remove(packetStandID: Int){
        val itmap = attached.iterator()
        outerloop@ while (itmap.hasNext()){
            val list = itmap.next().value
            val itlist = list.iterator()
            while (itlist.hasNext()){
                val pair = itlist.next()
                if (pair.first != packetStandID)
                    continue

                itlist.remove()

                if (list.isEmpty()){
                    itmap.remove()
                    break@outerloop
                }
            }
        }
    }
}

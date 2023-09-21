package com.github.justadeni.standapi.attachment

import com.github.justadeni.standapi.PacketStand
import com.github.justadeni.standapi.datatype.Offset
import com.github.justadeni.standapi.serialization.OffsetSerializer
import com.github.justadeni.standapi.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.entity.Entity
import java.util.UUID
@Serializable
class Attacher private constructor(){

    companion object {
        @Transient
        val instance:Attacher by lazy {
            Attacher()
        }
    }

    private val attached = HashMap<@Serializable(with = UUIDSerializer::class) UUID, MutableList<Pair<Int,@Serializable(with = OffsetSerializer::class) Offset>>>()

    fun PacketStand.attachTo(entity: Entity) {
        this.attachTo(entity, Offset.ZERO)
    }

    fun PacketStand.attachTo(entity: Entity, offset: Offset) {
        val key = entity.uniqueId

        remove(this.id)

        this.setLocation(entity.location)

        if (!attached.containsKey(key)) {
            attached[key] = mutableListOf(Pair(this.id, offset))
            return
        }

        attached[key]?.add(Pair(this.id, offset))
    }

    fun PacketStand.detach(){
        remove(this.id)
    }

    internal fun getMap(): Map<UUID, MutableList<Pair<Int, Offset>>> = attached

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

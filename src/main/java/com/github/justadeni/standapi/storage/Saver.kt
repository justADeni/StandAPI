package com.github.justadeni.standapi.storage

import com.github.justadeni.standapi.PacketStand
import com.github.justadeni.standapi.Ranger
import com.github.justadeni.standapi.StandAPI
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files

object Saver {
    private val file = File(StandAPI.getPlugin().dataFolder.path + "/Stands.yml").also { it.createNewFile() }
    internal fun saveAll(){
        if (!Config.savingEnabled)
            return

        file.printWriter().use {out ->
            Ranger.getAllStands().forEach{
                out.println(Json.encodeToString(it))
            }
        }
    }
    internal fun loadAll(){
        if (!Config.savingEnabled)
            return

        Files.readAllLines(file.toPath()).forEach{
            Ranger.add(Json.decodeFromString(it) as PacketStand)
        }
    }
}
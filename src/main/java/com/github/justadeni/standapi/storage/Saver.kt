package com.github.justadeni.standapi.storage

import com.github.justadeni.standapi.PacketStand
import com.github.justadeni.standapi.Ranger
import com.github.justadeni.standapi.StandAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files

/**
 * @suppress
 */
object Saver {

    private val file = File(StandAPI.plugin().dataFolder.path + "/Stands.yml").also { it.createNewFile() }

    internal suspend fun saveAll(){
        if (!StandApiConfig.savingEnabled)
            return

        file.printWriter().use {out ->
            Ranger.getAllStands().forEach{
                out.println(Json.encodeToString(it))
            }
        }
    }
    internal suspend fun loadAll() = withContext(Dispatchers.IO){
        if (!StandApiConfig.savingEnabled)
            return@withContext

        Files.readAllLines(file.toPath()).forEach{
            Ranger.add(Json.decodeFromString(it) as PacketStand)
        }
    }
}
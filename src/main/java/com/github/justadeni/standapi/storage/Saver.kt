package com.github.justadeni.standapi.storage

import com.github.justadeni.standapi.PacketStand
import com.github.justadeni.standapi.Ranger
import com.github.justadeni.standapi.StandAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.PrintWriter
import java.nio.file.Files

/**
 * @suppress
 */
object Saver {

    private val file = File(StandAPI.plugin().dataFolder.path + "/stands.yml").also { it.createNewFile() }

    internal fun saveAll(){
        if (!StandApiConfig.savingEnabled)
            return

        PrintWriter(file).close()

        file.printWriter().use {out ->
            Ranger.getAllStands().forEach{
                out.println(Json.encodeToString(it))
            }
        }
    }

    internal suspend fun loadAll(){
        if (!StandApiConfig.savingEnabled)
            return

        Files.readAllLines(file.toPath()).forEach{
            Json.decodeFromString(it) as PacketStand
            StandAPI.log("loading $it")
        }
    }
}
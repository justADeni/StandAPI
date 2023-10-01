package com.github.justadeni.standapi.storage

import com.github.justadeni.standapi.PacketStand
import com.github.justadeni.standapi.StandManager
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
        if (!StandApiConfig.getSavingEnabled())
            return

        file.copyTo(File(StandAPI.plugin().dataFolder.path + "/backup.yml"), true)
        PrintWriter(file).close()

        file.printWriter().use {out ->
            StandManager.getAllStands().forEach{
                out.println(Json.encodeToString(it))
            }
        }
    }

    internal suspend fun loadAll(){
        if (!StandApiConfig.getSavingEnabled())
            return

        withContext(Dispatchers.IO) { Files.readAllLines(file.toPath())}.forEach {
            Json.decodeFromString(it) as PacketStand
        }

    }
}
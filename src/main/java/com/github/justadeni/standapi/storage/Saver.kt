package com.github.justadeni.standapi.storage

import com.github.justadeni.standapi.PacketStand
import com.github.justadeni.standapi.StandAPI
import com.github.justadeni.standapi.StandManager
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    private val folder = StandAPI.plugin().dataFolder.also { it.mkdirs() }

    internal fun tickingSaving() = StandAPI.plugin().launch(Dispatchers.IO){
        delay(18_000.ticks) //15 minutes
        saveAll()
    }

    internal fun saveAll(){
        val groupedByPlugin = StandManager.getAllStands().groupBy { it.pluginName }.toMutableMap()
        groupedByPlugin.remove("None")
        groupedByPlugin.keys.forEach {
            val file = File(folder.path + "/$it.yml").also { it.createNewFile() }
            PrintWriter(file).close()
            file.printWriter().use {out ->
                groupedByPlugin[it]!!.forEach{
                    out.println(Json.encodeToString(it))
                }
            }
        }
    }

    internal suspend fun loadAll() = withContext(Dispatchers.IO){
        Files.list(folder.toPath()).forEach { path ->
            Files.readAllLines(path).forEach {
                val stand = Json.decodeFromString(it) as PacketStand
                stand.pluginName = path.fileName.toString().removeSuffix(".yml")
            }
        }
    }
}
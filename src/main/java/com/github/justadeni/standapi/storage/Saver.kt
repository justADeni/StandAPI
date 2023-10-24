package com.github.justadeni.standapi.storage

import com.github.justadeni.standapi.PacketStand
import com.github.justadeni.standapi.StandAPI
import com.github.justadeni.standapi.StandManager
import com.github.justadeni.standapi.misc.Logger
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.PrintWriter
import java.nio.file.Files
import kotlin.io.path.name

/**
 * @suppress
 */
object Saver {

    private val folder = StandAPI.plugin().dataFolder.also { it.mkdirs() }

    internal fun tickingSaving() = StandAPI.plugin().launch(Dispatchers.IO) {
        delay(18_000.ticks) //15 minutes
        saveAll()
    }

    internal fun saveAll() {
        val groupedByPlugin = StandManager.all().join().groupBy { it.pluginName }.toMutableMap()
        groupedByPlugin.remove("None")
        groupedByPlugin.keys.forEach {
            val file = File(folder.path + "/$it.yml").also { it.createNewFile() }
            PrintWriter(file).close()
            file.printWriter().use { out ->
                groupedByPlugin[it]!!.forEach {
                    out.println(Json.encodeToString(it))
                }
            }
        }
    }

    internal suspend fun loadAll() = withContext(Dispatchers.IO) {
        Files.list(folder.toPath()).forEach { path ->
            val lines = Files.readAllLines(path)
            for (i in 0 until lines.size) {
                try {
                    val stand = Json.decodeFromString(lines[i]) as PacketStand
                    stand.pluginName = path.fileName.toString().removeSuffix(".yml")
                } catch (e: SerializationException) {
                    Logger.warn("Error when decoding PacketStands in file ${path.name} on line number $i -> ${lines[i]}")
                    continue
                } catch (e: IllegalArgumentException) {
                    Logger.warn("Error when casting PacketStands in file ${path.name} on line number $i -> ${lines[i]}")
                    continue
                }
            }
        }
    }
}

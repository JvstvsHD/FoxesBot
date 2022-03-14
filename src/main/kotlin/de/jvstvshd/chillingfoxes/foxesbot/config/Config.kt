package de.jvstvshd.chillingfoxes.foxesbot.config

import de.jvstvshd.chillingfoxes.foxesbot.config.data.ConfigData
import de.jvstvshd.chillingfoxes.foxesbot.config.data.DataBaseData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.fileSize
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

class Config(private val path: Path = Path.of("config.json")) {

    private val json: Json = Json {
        prettyPrint = true
    }
    lateinit var configData: ConfigData

    @OptIn(ExperimentalSerializationApi::class)
    fun load() {
        create()
        configData = json.decodeFromStream(path.inputStream())
    }

    private fun create() {
        if (path.parent != null) {
            Files.createDirectories(path.parent)
        }
        if (!Files.exists(path)) {
            Files.createFile(path)
            configData = ConfigData(DataBaseData())
            save()
        }
        if (path.fileSize() <= 0) {
            configData = ConfigData(DataBaseData())
            save()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun save() {
        json.encodeToStream(configData, path.outputStream())
    }
}
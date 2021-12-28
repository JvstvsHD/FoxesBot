package de.jvstvshd.foxesbot.config

import com.fasterxml.jackson.databind.ObjectMapper
import de.jvstvshd.foxesbot.config.data.ConfigData
import de.jvstvshd.foxesbot.config.data.DataBaseData
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.fileSize

class Config(private val path: Path = Path.of("config.json")) {

    private val objectMapper = ObjectMapper()
    lateinit var configData: ConfigData

    fun load() {
        create()
        configData = objectMapper.readValue(path.toFile(), ConfigData::class.java)
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

    fun save() {
        objectMapper.writerWithDefaultPrettyPrinter().writeValues(path.toFile()).write(configData)
    }
}
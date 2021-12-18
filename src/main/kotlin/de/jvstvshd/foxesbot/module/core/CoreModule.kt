package de.jvstvshd.foxesbot.module.core

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.zaxxer.hikari.HikariDataSource
import de.jvstvshd.foxesbot.config.Config
import de.jvstvshd.foxesbot.module.core.commands.commands
import de.jvstvshd.foxesbot.module.music.MusicService

class CoreModule(val config: Config, val dataSource: HikariDataSource) : Extension() {

    override val name = "core"
    override val bundle = "core"
    val service = MusicService(dataSource)

    override suspend fun setup() {
        commands()
    }
}
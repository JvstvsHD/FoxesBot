package de.jvstvshd.foxesbot.module.core

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.zaxxer.hikari.HikariDataSource
import de.jvstvshd.foxesbot.config.Config
import de.jvstvshd.foxesbot.module.core.commands.commands

class CoreModule(val config: Config, val dataSource: HikariDataSource) : Extension() {

    override val name = "core"
    override val bundle = "core"

    override suspend fun setup() {
        commands()
    }
}
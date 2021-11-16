package de.jvstvshd.foxesbot.module.core

import com.kotlindiscord.kord.extensions.extensions.Extension
import de.jvstvshd.foxesbot.config.Config
import de.jvstvshd.foxesbot.module.core.commands.commands

class CoreModule(private val config: Config) : Extension() {

    override val name = "core"
    override val bundle = "core"

    override suspend fun setup() {
        commands(config)
    }
}
package de.jvstvshd.foxesbot.module.core.commands

import de.jvstvshd.foxesbot.config.Config
import de.jvstvshd.foxesbot.module.core.CoreModule

suspend fun CoreModule.commands(config: Config) {
    infoCommand()
    exitCommand()
    helpCommand(config)
}
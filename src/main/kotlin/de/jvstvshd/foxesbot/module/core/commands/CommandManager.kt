package de.jvstvshd.foxesbot.module.core.commands

import de.jvstvshd.foxesbot.module.core.CoreModule

suspend fun CoreModule.commands() {
    infoCommand()
    exitCommand()
    helpCommand()
    settingsCommand()
    restartCommand()
}
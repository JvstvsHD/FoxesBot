package de.jvstvshd.chillingfoxes.foxesbot.module.core.commands

import de.jvstvshd.chillingfoxes.foxesbot.module.core.CoreModule

suspend fun CoreModule.commands() {
    infoCommand()
    exitCommand()
    helpCommand()
    settingsCommand()
    musicCommands()
    restartCommand()
    testCommand()
}
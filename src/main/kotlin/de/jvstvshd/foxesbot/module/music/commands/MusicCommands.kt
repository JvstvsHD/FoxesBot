package de.jvstvshd.foxesbot.module.music.commands

import de.jvstvshd.foxesbot.module.music.MusicModule
import io.github.qbosst.kordex.commands.hybrid.publicHybridCommand

suspend fun MusicModule.songCommand() = publicHybridCommand {
    name = "song"
    description = "Gibt den derzeit gespielten Song an"
    action {
        respond {

        }
    }
}

suspend fun MusicModule.musicCommands() {

}
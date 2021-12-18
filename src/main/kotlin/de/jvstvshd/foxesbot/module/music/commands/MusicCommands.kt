package de.jvstvshd.foxesbot.module.music.commands

import com.kotlindiscord.kord.extensions.extensions.chatCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.jvstvshd.foxesbot.module.music.MusicModule
import de.jvstvshd.foxesbot.module.music.player.musicPlayers
import de.jvstvshd.foxesbot.module.music.player.trackInfo
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.reply
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.create.embed

suspend fun MusicModule.songCommand() = publicSlashCommand {
    name = "song"
    description = "Gibt den derzeit gespielten Song an"
    action {
        respond(getSong(guild!!.id))
    }
}

suspend fun MusicModule.songChatCommand() = chatCommand {
    name = "song"
    description = "Gibt den derzeit gespielten Song an"
    action {
        message.reply(getSong(guild!!.id))
    }
}

suspend fun MusicModule.musicCommands() {
    songCommand()
    songChatCommand()
}

fun getSong(guildId: Snowflake, time: Boolean = true): MessageCreateBuilder.() -> Unit = {
    embed(musicPlayers[guildId].trackInfo(time))
}
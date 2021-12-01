package de.jvstvshd.foxesbot.module.christmas.commands

import com.kotlindiscord.kord.extensions.DISCORD_FUCHSIA
import com.kotlindiscord.kord.extensions.extensions.chatCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.respond
import de.jvstvshd.foxesbot.module.christmas.ChristmasModule
import de.jvstvshd.foxesbot.util.KordUtil
import de.jvstvshd.foxesbot.util.limit.CountBasedLimitation
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.create.embed
import kotlinx.datetime.Clock

suspend fun ChristmasModule.christmasMusicChatCommand(name: String) = chatCommand {
    this.name = name
    description = "Spielt Weihnachtsmusik"
    action {
        val channel = member?.getVoiceState()?.getChannelOrNull()
        if (channel == null) {
            message.respond {
                content = "Bitte verbinde dich zuerst!"
            }
            return@action
        } else {
            val voiceState = this@chatCommand.kord.getSelf().asMember(guild!!.id).getVoiceStateOrNull()
            if (voiceState?.channelId != null) {
                message.respond {
                    content = "Der Bot ist bereits mit einem Channel verbunden!"
                }
                return@action
            }
            val musicPlayer = createMusicPlayer(channel, CountBasedLimitation(2))
            //ChristmasMusicPlayer(channel as VoiceChannel, MusicService(dataSource), CountBasedLimitation(2))
            try {
                musicPlayer.playRandom("christmas")
                message.respond(useReply = true, pingInReply = false, getSong(guild!!.id))
            } catch (e: NoSuchElementException) {
                message.respond {
                    content = "Es ist kein Element vorhanden, das abgespielt werden könnte."
                }
                return@action
            }
        }
    }
}

@OptIn(KordVoice::class)
suspend fun ChristmasModule.christmasMusicCommand(name: String) = publicSlashCommand {
    this.name = name
    description = "Spielt Weihnachtsmusik"
    action {
        val channel = member?.getVoiceState()?.getChannelOrNull()
        if (channel == null) {
            respond {
                content = "Bitte verbinde dich zuerst!"
            }
            return@action
        } else {
            val voiceState = this@publicSlashCommand.kord.getSelf().asMember(guild!!.id).getVoiceStateOrNull()
            if (voiceState?.channelId != null) {
                respond {
                    content = "Der Bot ist bereits mit einem Channel verbunden!"
                }
                return@action
            }
            val musicPlayer = createMusicPlayer(channel, CountBasedLimitation(2))
            //ChristmasMusicPlayer(channel as VoiceChannel, MusicService(dataSource), CountBasedLimitation(2))
            try {
                musicPlayer.playRandom("christmas")
                respond(getSong(guild!!.id))
            } catch (e: NoSuchElementException) {
                respond {
                    content = "Es ist kein Element vorhanden, das abgespielt werden könnte."
                }
                return@action
            }
        }
    }
}

suspend fun ChristmasModule.songCommand() = publicSlashCommand {
    name = "song"
    description = "Zeigt an, welcher Song derzeit gespielt"
    action {
        respond(getSong(guild!!.id))
    }
}

suspend fun ChristmasModule.songChatCommand() = chatCommand {
    name = "song"
    description = "Zeigt an, welcher Song derzeit gespielt"
    action {
        message.respond(useReply = true, pingInReply = false, getSong(guild!!.id))
    }
}


fun ChristmasModule.getSong(guildId: Snowflake): MessageCreateBuilder.() -> Unit =
    {
        embed {
            val track = christmasTimes[guildId]?.currentTrack
            if (track == null) {
                title = "Es wird derzeit kein Song gespielt."
            } else {
                title = track.info.title
                url = track.info.uri
                thumbnail {
                    url = "https://img.youtube.com/vi/${track.info.identifier}/0.jpg"
                }
                color = DISCORD_FUCHSIA
            }
            footer = KordUtil.createFooter("Weihnachtsmusik 2021")
            timestamp = Clock.System.now()
        }
    }

suspend fun ChristmasModule.christmasMusicCommands() {
    christmasMusicCommand("wm")
    christmasMusicCommand("weihnachtsmusik")
    christmasMusicChatCommand("wm")
    christmasMusicChatCommand("weihnachtsmusik")
    songChatCommand()
    songCommand()
}
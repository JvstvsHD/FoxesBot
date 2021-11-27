package de.jvstvshd.foxesbot.module.christmas.commands

import com.kotlindiscord.kord.extensions.extensions.chatCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.respond
import de.jvstvshd.foxesbot.module.christmas.ChristmasModule
import de.jvstvshd.foxesbot.util.limit.CountBasedLimitation
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake

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
                val track = musicPlayer.playRandom("christmas")
                message.respond {
                    content = "Es wird gespielt: " + "[" + track.info.title + "](<" + track.info.uri + ">)"
                }
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
                val track = musicPlayer.playRandom("christmas")
                respond {
                    content = "Es wird gespielt: " + "[" + track.info.title + "](<" + track.info.uri + ">)"
                }
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
        respond {
            content = getSong(guild!!.id)
        }
    }
}

suspend fun ChristmasModule.songChatCommand() = chatCommand {
    name = "song"
    description = "Zeigt an, welcher Song derzeit gespielt"
    action {
        message.respond {
            content = getSong(guild!!.id)
        }
    }
}

fun ChristmasModule.getSong(guildId: Snowflake): String {
    val track = christmasTimes[guildId]?.currentTrack ?: return "Es wird derzeit kein Song gespielt"
    return "\\[" + track.info.title + "\\]\\(" + track.info.uri + "\\)"
}

suspend fun ChristmasModule.christmasMusicCommands() {
    christmasMusicCommand("wm")
    christmasMusicCommand("weihnachtsmusik")
    christmasMusicChatCommand("wm")
    christmasMusicChatCommand("weihnachtsmusik")
    songChatCommand()
    songCommand()
}
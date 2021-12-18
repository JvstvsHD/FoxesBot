package de.jvstvshd.foxesbot.module.christmas.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingInt
import com.kotlindiscord.kord.extensions.extensions.chatCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.respond
import de.jvstvshd.foxesbot.module.christmas.ChristmasModule
import de.jvstvshd.foxesbot.module.christmas.music.ChristmasMusicPlayerGenerator
import de.jvstvshd.foxesbot.module.christmas.statistic.UserBotMoves
import de.jvstvshd.foxesbot.module.music.player.musicPlayers
import de.jvstvshd.foxesbot.module.music.player.trackInfo
import de.jvstvshd.foxesbot.util.KordUtil
import de.jvstvshd.foxesbot.util.limit.IntBasedLimitation
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.behavior.reply
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.create.embed
import kotlinx.datetime.Clock
import java.awt.Color

class ChristmasMusicArgs : Arguments() {
    val songs by defaultingInt("songs", "Anzahl der Songs", 2)
}

suspend fun ChristmasModule.christmasMusicChatCommand(name: String) = chatCommand(::ChristmasMusicArgs) {
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
            println(arguments.songs)
            message.reply(play(channel, member!!, arguments.songs))
        }
    }
}

@OptIn(KordVoice::class)
suspend fun ChristmasModule.christmasMusicCommand(name: String) = publicSlashCommand(::ChristmasMusicArgs) {
    this.name = name
    description = "Spielt Weihnachtsmusik"
    action {
        val channel = member?.getVoiceStateOrNull()?.getChannelOrNull()
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
            respond(play(channel, member!!, arguments.songs))
        }
    }
}

private suspend fun ChristmasModule.play(
    channel: BaseVoiceChannelBehavior,
    member: MemberBehavior,
    limit: Int
): MessageCreateBuilder.() -> Unit {
    if (limit > 10) {
        return {
            embed {
                title = "Fehler"
                description = "Es sind derzeit max. 10 Songs am Stück erlaubt. Deine Angabe: $limit"
                footer {
                    text = "Weihnachtsmusik 2021"
                }
                timestamp = Clock.System.now()
                color = KordUtil.convertColor(Color.RED)
            }
        }
    }
    val musicPlayer =
        ChristmasMusicPlayerGenerator(channel, musicService, IntBasedLimitation(limit)).createMusicPlayer()
    if (musicPlayer.started) {
        musicPlayer.exit()
    }
    try {
        musicPlayer.playRandom("christmas")
        statisticService.log(UserBotMoves, member.id, 1)
        return getSong(channel.guildId, false)
    } catch (e: NoSuchElementException) {
        return {
            content = "Es ist kein Element vorhanden, das abgespielt werden könnte."
        }
    }
}

fun getSong(guildId: Snowflake, time: Boolean = true): MessageCreateBuilder.() -> Unit = {
    embed(musicPlayers[guildId].trackInfo(time, "Weihnachtsmusik 2021"))
}

suspend fun ChristmasModule.christmasMusicCommands() {
    christmasMusicCommand("wm")
    christmasMusicCommand("weihnachtsmusik")
    christmasMusicChatCommand("wm")
    christmasMusicChatCommand("weihnachtsmusik")
}


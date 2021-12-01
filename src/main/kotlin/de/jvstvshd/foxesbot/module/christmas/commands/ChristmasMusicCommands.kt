package de.jvstvshd.foxesbot.module.christmas.commands

import com.kotlindiscord.kord.extensions.DISCORD_FUCHSIA
import com.kotlindiscord.kord.extensions.extensions.chatCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.respond
import de.jvstvshd.foxesbot.module.christmas.ChristmasModule
import de.jvstvshd.foxesbot.module.christmas.UserBotMoves
import de.jvstvshd.foxesbot.util.KordUtil
import de.jvstvshd.foxesbot.util.limit.CountBasedLimitation
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.rest.builder.message.EmbedBuilder
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
            message.respond(useReply = true, pingInReply = false, playSuspended(channel, member!!))
        }
    }
}

@OptIn(KordVoice::class)
suspend fun ChristmasModule.christmasMusicCommand(name: String) = publicSlashCommand {
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
            respond(play(channel, member!!))
        }
    }
}

private suspend fun ChristmasModule.play(
    channel: BaseVoiceChannelBehavior,
    member: MemberBehavior
): MessageCreateBuilder.() -> Unit {
    val musicPlayer = createMusicPlayer(channel, CountBasedLimitation(2))
    try {
        musicPlayer.playRandom("christmas")
        statisticService.log(UserBotMoves, member.id, 1)
        return getSong(channel.guildId)
    } catch (e: NoSuchElementException) {
        return {
            content = "Es ist kein Element vorhanden, das abgespielt werden könnte."
        }
    }
}

private suspend fun ChristmasModule.playSuspended(
    channel: BaseVoiceChannelBehavior,
    member: MemberBehavior
): suspend MessageCreateBuilder.() -> Unit {
    val musicPlayer = createMusicPlayer(channel, CountBasedLimitation(2))
    try {
        musicPlayer.playRandom("christmas")
        statisticService.log(UserBotMoves, member.id, 1)
        return getSongSuspended(channel.guildId)
    } catch (e: NoSuchElementException) {
        return {
            content = "Es ist kein Element vorhanden, das abgespielt werden könnte."
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
        message.respond(useReply = true, pingInReply = false, getSongSuspended(guild!!.id))
    }
}

fun ChristmasModule.getSongSuspended(guildId: Snowflake): suspend MessageCreateBuilder.() -> Unit = {
    embed(buildEmbed(guildId))
}

private fun ChristmasModule.buildEmbed(guildId: Snowflake): EmbedBuilder.() -> Unit = {
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
        description = "${(track.position / 1000)}s/${track.info.length / 1000}s"
    }
    footer = KordUtil.createFooter("Weihnachtsmusik 2021")
    timestamp = Clock.System.now()
}

fun ChristmasModule.getSong(guildId: Snowflake): MessageCreateBuilder.() -> Unit =
    {
        embed(buildEmbed(guildId))
    }

suspend fun ChristmasModule.christmasMusicCommands() {
    christmasMusicCommand("wm")
    christmasMusicCommand("weihnachtsmusik")
    christmasMusicChatCommand("wm")
    christmasMusicChatCommand("weihnachtsmusik")
    songChatCommand()
    songCommand()
}
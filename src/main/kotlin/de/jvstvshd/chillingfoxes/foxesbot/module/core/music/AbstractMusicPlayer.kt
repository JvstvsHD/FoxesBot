/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.core.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.kord.common.annotation.KordVoice
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.exception.GatewayNotFoundException
import dev.kord.voice.AudioFrame
import dev.kord.voice.VoiceConnection

abstract class AbstractMusicPlayer(override val channel: BaseVoiceChannelBehavior, override val service: MusicService) :
    MusicPlayer {

    val kord = channel.kord
    val guild = channel.guild

    @OptIn(KordVoice::class)
    var connection: VoiceConnection? = null

    @OptIn(KordVoice::class)
    override suspend fun connect(player: AudioPlayer): VoiceConnection = openConnection(player).also {
        connection = it
    }

    @OptIn(KordVoice::class)
    override suspend fun openConnection(player: AudioPlayer): VoiceConnection {
        val connection = VoiceConnection(
            guild.gateway ?: GatewayNotFoundException.voiceConnectionGatewayNotFound(channel.guildId),
            kord.selfId,
            channel.id,
            guild.id
        ) {
            audioProvider {
                AudioFrame.fromData(provideAudio(player))
            }
        }
        connection.connect()
        return connection
    }

    open suspend fun provideAudio(player: AudioPlayer? = null): ByteArray? =
        player?.provide()?.data


    @OptIn(KordVoice::class)
    override suspend fun exit(force: Boolean): AudioTrack? {
        println("exit for channel: ${channel.id}")
        val track =
            if (connection != null) {
                connection?.shutdown()
                exit0()
            } else {
                currentTrack
            }
        return track
    }

    open suspend fun exit0(player: AudioPlayer? = null): AudioTrack? = null
}
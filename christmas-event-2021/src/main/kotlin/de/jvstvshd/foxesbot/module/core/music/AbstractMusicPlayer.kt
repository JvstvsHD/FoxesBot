package de.jvstvshd.foxesbot.module.core.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.kord.common.annotation.KordVoice
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.voice.AudioFrame
import dev.kord.voice.VoiceConnection

abstract class AbstractMusicPlayer(override val channel: BaseVoiceChannelBehavior, override val service: MusicService) :
    MusicPlayer {

    @OptIn(KordVoice::class)
    var connection: VoiceConnection? = null

    @OptIn(KordVoice::class)
    override suspend fun connect(player: AudioPlayer): VoiceConnection = openConnection(player).also {
        connection = it
    }

    @OptIn(KordVoice::class)
    override suspend fun openConnection(player: AudioPlayer): VoiceConnection =
        channel.connect {
            audioProvider { AudioFrame.fromData(provideAudio(player)) }
        }

    open suspend fun provideAudio(player: AudioPlayer? = null): ByteArray? {
        if (player != null) {
            return player.provide()?.data
        }
        return null
    }

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
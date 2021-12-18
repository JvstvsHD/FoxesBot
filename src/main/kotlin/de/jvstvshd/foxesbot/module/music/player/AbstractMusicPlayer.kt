package de.jvstvshd.foxesbot.module.music.player

import com.kotlindiscord.kord.extensions.DISCORD_FUCHSIA
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import de.jvstvshd.foxesbot.module.music.MusicService
import de.jvstvshd.foxesbot.module.music.MusicTrackInfo
import de.jvstvshd.foxesbot.util.KordUtil
import dev.kord.common.annotation.KordVoice
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.voice.AudioFrame
import dev.kord.voice.VoiceConnection
import kotlinx.datetime.Clock

abstract class AbstractMusicPlayer(override val channel: BaseVoiceChannelBehavior, override val service: MusicService) :
    MusicPlayer {

    var initialized = false
        private set

    override fun init() {
        musicPlayers[channel.guildId] = this
        synchronized(initialized) {
            if (initialized) {
                throw IllegalArgumentException("already initialized")
            }
            initialized = true
        }
    }

    override fun trackInfo0(time: Boolean): EmbedBuilder.() -> Unit =
        if (trackInfo == de.jvstvshd.foxesbot.module.music.noSongInfo || trackInfo.currentTrack == null) {
            noSongInfo
        } else {
            getTrackInfo(time, trackInfo)
        }


    private fun getTrackInfo(time: Boolean, info: MusicTrackInfo): EmbedBuilder.() -> Unit = {
        val track = info.currentTrack!!
        title = track.info.title
        url = track.info.uri
        track.let {
            if (track.sourceManager is YoutubeAudioSourceManager) {
                thumbnail {
                    url = "https://img.youtube.com/vi/${it.info.identifier}/0.jpg"
                }
            }
            if (time) {
                description =
                    "${formatTime(it.position / 1000)}/${formatTime(it.info.length / 1000)}"
            }
        }
        color = DISCORD_FUCHSIA

        footer = KordUtil.createFooter("Weihnachtsmusik 2021")
        timestamp = Clock.System.now()
    }

    @OptIn(KordVoice::class)
    var connection: VoiceConnection? = null

    private fun checkInit() {
        if (!initialized) {
            throw IllegalArgumentException("not yet initialized")
        }
    }

    @OptIn(KordVoice::class)
    override suspend fun connect(player: AudioPlayer): VoiceConnection = openConnection(player).also {
        connection = it
    }

    @OptIn(KordVoice::class)
    override suspend fun openConnection(player: AudioPlayer): VoiceConnection {
        checkInit()
        return channel.connect {
            audioProvider { AudioFrame.fromData(provideAudio(player)) }
        }
    }

    open suspend fun provideAudio(player: AudioPlayer? = null): ByteArray? {
        if (player != null) {
            return player.provide()?.data
        }
        return null
    }

    @OptIn(KordVoice::class)
    override suspend fun exit(force: Boolean): MusicTrackInfo {
        println("exit for channel: ${channel.id}")
        val track =
            if (connection != null) {
                connection?.shutdown()
                exit0()
            } else {
                trackInfo
            }
        return track
    }

    open suspend fun exit0(player: AudioPlayer? = null): MusicTrackInfo = de.jvstvshd.foxesbot.module.music.noSongInfo

    private fun formatTime(original: Long): String {
        val minutes: Long
        val seconds: Long = original % 60
        minutes = if (original >= 60) {
            (original - seconds) / 60
        } else {
            0
        }
        return "${formatTimeField(minutes)}:${formatTimeField(seconds)}"
    }

    private fun formatTimeField(value: Long): String {
        if (value < 10) {
            return "0$value"
        }
        return value.toString()
    }
}
package de.jvstvshd.foxesbot.module.music.player

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import de.jvstvshd.foxesbot.module.music.MusicService
import de.jvstvshd.foxesbot.module.music.MusicState
import de.jvstvshd.foxesbot.module.music.MusicTrackInfo
import de.jvstvshd.foxesbot.util.KordUtil
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.voice.VoiceConnection
import kotlinx.datetime.Clock
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

val musicPlayers: MutableMap<Snowflake, MusicPlayer> = mutableMapOf()
val noSongInfo: EmbedBuilder.() -> Unit = {
    title = "Es wird derzeit kein Song gespielt."
    footer = KordUtil.createFooter("Weihnachtsmusik 2021")
    timestamp = Clock.System.now()
}

fun MusicPlayer?.trackInfo(time: Boolean): EmbedBuilder.() -> Unit =
    this?.trackInfo0(time) ?: noSongInfo

interface MusicPlayer {

    var trackInfo: MusicTrackInfo
    val channel: BaseVoiceChannelBehavior
    val service: MusicService

    fun init()

    suspend fun play(url: String): MusicTrackInfo

    suspend fun exit(force: Boolean = false): MusicTrackInfo

    @OptIn(KordVoice::class)
    suspend fun connect(player: AudioPlayer): VoiceConnection

    @OptIn(KordVoice::class)
    suspend fun openConnection(player: AudioPlayer): VoiceConnection

    suspend fun playRandom(list: List<String>): MusicTrackInfo = play(list.random())

    suspend fun playRandom(topic: String): MusicTrackInfo =
        playRandom(service.getUrls(topic))

    suspend fun AudioPlayerManager.playTrack(
        query: String,
        player: AudioPlayer,
        topic: String = "unknown"
    ): MusicTrackInfo {
        val track = suspendCoroutine<AudioTrack> {
            this.loadItem(query, object : AudioLoadResultHandler {

                override fun trackLoaded(track: AudioTrack) {
                    it.resume(track)
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    it.resume(playlist.tracks.first())
                }

                override fun noMatches() {
                    TODO()
                }

                override fun loadFailed(exception: FriendlyException?) {
                    TODO()
                }
            })
        }

        player.playTrack(track)

        return MusicTrackInfo(track.info.title, track.info.uri, MusicState.ACTIVATED, topic, track)
    }

    fun trackInfo0(time: Boolean): EmbedBuilder.() -> Unit
}
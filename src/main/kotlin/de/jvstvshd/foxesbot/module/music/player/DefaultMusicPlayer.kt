package de.jvstvshd.foxesbot.module.music.player

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import de.jvstvshd.foxesbot.module.music.MusicService
import de.jvstvshd.foxesbot.module.music.MusicTrackInfo
import dev.kord.common.annotation.KordVoice
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior

open class DefaultMusicPlayer(override val channel: BaseVoiceChannelBehavior, override val service: MusicService) :
    AbstractMusicPlayer(channel, service) {
    override var trackInfo: MusicTrackInfo = de.jvstvshd.foxesbot.module.music.noSongInfo
    private val lavaplayerManager: AudioPlayerManager = DefaultAudioPlayerManager()

    init {
        AudioSourceManagers.registerRemoteSources(lavaplayerManager)
    }

    @OptIn(KordVoice::class)
    override suspend fun play(url: String): MusicTrackInfo {
        return play0(url)
    }

    @OptIn(KordVoice::class)
    suspend fun play0(url: String): MusicTrackInfo {
        val player = configurePlayer(lavaplayerManager)
        val track = lavaplayerManager.playTrack(url, player)
        connect(player)
        return track
    }


    open fun configurePlayer(manager: AudioPlayerManager): AudioPlayer = manager.createPlayer()
}
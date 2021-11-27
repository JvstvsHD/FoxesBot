package de.jvstvshd.foxesbot.module.core.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.kord.common.annotation.KordVoice
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior

open class DefaultMusicPlayer(override val channel: BaseVoiceChannelBehavior, override val service: MusicService) :
    AbstractMusicPlayer(channel, service) {
    override var currentTrack: AudioTrack? = null
    val lavaplayerManager: AudioPlayerManager = DefaultAudioPlayerManager()

    init {
        AudioSourceManagers.registerRemoteSources(lavaplayerManager)
    }

    @OptIn(KordVoice::class)
    override suspend fun play(url: String): AudioTrack {
        return play0(url)
    }

    @OptIn(KordVoice::class)
    suspend fun play0(url: String): AudioTrack {
        // our lavaplayer audio player which will provide frames of audio
        val player = configurePlayer(lavaplayerManager)
        // lavaplayer uses ytsearch: as an identifier to search for YouTube
        val track = lavaplayerManager.playTrack(url, player)
        // here we actually connect to the voice channel
        connect(player)
        return track
    }


    open fun configurePlayer(manager: AudioPlayerManager): AudioPlayer = manager.createPlayer()
}
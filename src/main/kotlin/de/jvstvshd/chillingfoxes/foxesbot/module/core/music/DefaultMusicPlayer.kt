/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.core.music

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
    private val lavaplayerManager: AudioPlayerManager = DefaultAudioPlayerManager()

    init {
        AudioSourceManagers.registerRemoteSources(lavaplayerManager)
    }

    @OptIn(KordVoice::class)
    override suspend fun play(url: String): AudioTrack {
        return play0(url)
    }

    @OptIn(KordVoice::class)
    suspend fun play0(url: String): AudioTrack {
        val player = configurePlayer(lavaplayerManager)
        val track = lavaplayerManager.playTrack(url, player)
        connect(player)
        return track
    }


    open fun configurePlayer(manager: AudioPlayerManager): AudioPlayer = manager.createPlayer()
}
/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.core.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.kord.common.annotation.KordVoice
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.voice.VoiceConnection
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface MusicPlayer {

    var currentTrack: AudioTrack?
    val channel: BaseVoiceChannelBehavior
    val service: MusicService

    suspend fun play(url: String): AudioTrack

    suspend fun exit(force: Boolean = false): AudioTrack?

    @OptIn(KordVoice::class)
    suspend fun connect(player: AudioPlayer): VoiceConnection

    @OptIn(KordVoice::class)
    suspend fun openConnection(player: AudioPlayer): VoiceConnection

    suspend fun playRandom(list: List<String>): AudioTrack = play(list.random())

    suspend fun playRandom(topic: String): AudioTrack =
        playRandom(service.getMusicEntities().map { music -> music.url })

    suspend fun AudioPlayerManager.playTrack(query: String, player: AudioPlayer): AudioTrack {
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

        return track
    }
}
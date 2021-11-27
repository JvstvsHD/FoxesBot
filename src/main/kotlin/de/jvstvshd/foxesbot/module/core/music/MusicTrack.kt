package de.jvstvshd.foxesbot.module.core.music

import com.zaxxer.hikari.HikariDataSource
import dev.kord.core.entity.channel.VoiceChannel

class MusicTrack(
    private val url: String,
    private val name: String,
    private val state: MusicState,
    private val dataSource: HikariDataSource
) {

    suspend fun play(channel: VoiceChannel) {
        val player = DefaultMusicPlayer(channel, MusicService(dataSource))
        player.play(url)
    }
}
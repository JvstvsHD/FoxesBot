package de.jvstvshd.foxesbot.module.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack

data class MusicTrackInfo(
    val name: String,
    val url: String,
    val state: MusicState,
    val topic: String,
    var currentTrack: AudioTrack? = null
)

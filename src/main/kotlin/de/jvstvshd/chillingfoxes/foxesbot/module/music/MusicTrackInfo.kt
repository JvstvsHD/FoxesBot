package de.jvstvshd.chillingfoxes.foxesbot.module.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack

val noSongInfo = MusicTrackInfo("Kein Song", "https://www.youtube.com/", MusicState.UNKNOWN, "nothing")

data class MusicTrackInfo(
    val name: String,
    val url: String,
    val state: MusicState,
    val topic: String,
    var currentTrack: AudioTrack? = null
)

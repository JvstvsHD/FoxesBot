package de.jvstvshd.foxesbot.module.christmas.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent
import de.jvstvshd.foxesbot.module.music.MusicService
import de.jvstvshd.foxesbot.module.music.MusicTrackInfo
import de.jvstvshd.foxesbot.module.music.noSongInfo
import de.jvstvshd.foxesbot.util.limit.Limitation
import de.jvstvshd.foxesbot.util.limit.UnlimitedLimitation
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.kordLogger
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.runBlocking

class ChristmasTimePlayer(
    override val channel: BaseVoiceChannelBehavior,
    override val service: MusicService,
    limitation: Limitation = UnlimitedLimitation()
) : ChristmasMusicPlayer(channel, service, limitation) {

    private var exitProcessStarted = false

    override fun configurePlayer(): AudioPlayer {
        val player = lavaplayerManager.createPlayer()
        lastPlayer = player
        player.addListener { event ->
            if (event is TrackEndEvent) {
                runBlocking {
                    kordLogger.debug("stopping ${event.track.info.title}")
                    try {
                        kordLogger.debug("limit: ${queue.limitation.limit()}, current: ${queue.limitation}, limit: ${queue.limitation.shouldLimit()}")
                        if (queue.limitation.shouldLimit()) {
                            if (exitProcessStarted) {
                                return@runBlocking
                            }
                            this@ChristmasTimePlayer.exit()
                        } else {
                            trackInfo = playNext(player!!)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return player
    }

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun exit(force: Boolean): MusicTrackInfo {
        lastPlayer?.stopTrack()
        trackInfo.currentTrack?.stop()
        kordLogger.debug("started: $started")
        if (!started && !force) {
            return noSongInfo
        }
        exitProcessStarted = true
        queue.limitation.limitNow()
        kordLogger.debug("Exit process started...")
        val player = lavaplayerManager.createPlayer()
        lastPlayer = player
        player.addListener { event ->
            runBlocking {
                if (event is TrackEndEvent) {
                    super.exit(false)
                    return@runBlocking
                }
            }
        }
        trackInfo =
            play0(
                "https://cdn.discordapp.com/attachments/654335565369442304/917471577480769606/2021-12-06_18-43-27.mp4",
                //"https://youtu.be/C9YYBvxb0Tc",
                player
            )
        return trackInfo
    }

    override suspend fun provideAudio(player: AudioPlayer?): ByteArray? {
        return lastPlayer?.provide()?.data
    }
}
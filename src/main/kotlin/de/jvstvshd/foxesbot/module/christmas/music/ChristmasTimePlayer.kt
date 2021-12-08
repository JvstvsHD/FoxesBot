package de.jvstvshd.foxesbot.module.christmas.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import de.jvstvshd.foxesbot.module.christmas.ChristmasModule
import de.jvstvshd.foxesbot.module.core.music.MusicService
import de.jvstvshd.foxesbot.util.limit.Limitation
import de.jvstvshd.foxesbot.util.limit.UnlimitedLimitation
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.runBlocking

class ChristmasTimePlayer(
    override val channel: BaseVoiceChannelBehavior,
    override val service: MusicService,
    module: ChristmasModule,
    limitation: Limitation = UnlimitedLimitation()
) : ChristmasMusicPlayer(channel, service, module, limitation) {

    private fun configurePlayer(): AudioPlayer {
        val player = lavaplayerManager.createPlayer()
        lastPlayer = player
        player.addListener { event ->
            if (event is TrackEndEvent) {
                runBlocking {
                    logger.debug("stopping ${event.track.info.title}")
                    try {
                        logger.debug("limit: ${queue.limitation.limit()}, current: ${queue.limitation.toString()}")
                        if (queue.limitation.shouldLimit()) {
                            if (exitProcessStarted) {
                                return@runBlocking
                            }
                            exit()
                        }
                        playNext(player!!)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return player
    }

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun exit(): AudioTrack? {
        lastPlayer?.stopTrack()
        currentTrack?.stop()
        if (!started) {
            return null
        }
        queue.limitation.limitNow()
        println("exit...")
        val player = lavaplayerManager.createPlayer()
        lastPlayer = player
        player.addListener { event ->
            runBlocking {
                if (event is TrackEndEvent) {
                    super.exit()
                    return@runBlocking
                }
            }
        }
        currentTrack =
            play0(
                "https://cdn.discordapp.com/attachments/654335565369442304/917471577480769606/2021-12-06_18-43-27.mp4",
                //"https://youtu.be/C9YYBvxb0Tc",
                player
            )
        return currentTrack
    }

    override suspend fun provideAudio(player: AudioPlayer?): ByteArray? {
        return lastPlayer?.provide()?.data
    }
}
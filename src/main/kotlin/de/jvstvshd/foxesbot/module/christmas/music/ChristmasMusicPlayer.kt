package de.jvstvshd.foxesbot.module.christmas.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import de.jvstvshd.foxesbot.module.christmas.ChristmasModule
import de.jvstvshd.foxesbot.module.core.music.AbstractMusicPlayer
import de.jvstvshd.foxesbot.module.core.music.MusicService
import de.jvstvshd.foxesbot.util.collection.LimitedDeque
import de.jvstvshd.foxesbot.util.limit.LimitExceededException
import de.jvstvshd.foxesbot.util.limit.Limitation
import de.jvstvshd.foxesbot.util.limit.UnlimitedLimitation
import dev.kord.common.annotation.KordVoice
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.entity.channel.StageChannel
import dev.kord.core.supplier.EntitySupplyStrategy
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.apache.logging.log4j.LogManager

class ChristmasMusicPlayer(
    override val channel: BaseVoiceChannelBehavior,
    override val service: MusicService,
    private val module: ChristmasModule,
    limitation: Limitation = UnlimitedLimitation()
) : AbstractMusicPlayer(channel, service) {

    private val lavaplayerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    override var currentTrack: AudioTrack? = null
    private val logger = LogManager.getLogger()
    private var exitProcessStarted = false
    private var lastPlayer: AudioPlayer? = null
    var started = false

    init {
        AudioSourceManagers.registerRemoteSources(lavaplayerManager)
    }

    private val queue = LimitedDeque<String>(limitation)

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun play(url: String): AudioTrack {
        started = true
        return playNext(configurePlayer(), true)
    }

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

    override suspend fun exit0(player: AudioPlayer?): AudioTrack? {
        module.christmasTimes.remove(channel.guildId)
        lavaplayerManager.shutdown()
        if (channel is StageChannel) {
            channel.getStageInstance().delete("Beendet.")
        }
        return currentTrack
    }

    private suspend fun playNext(player: AudioPlayer, refillQueue: Boolean = false): AudioTrack {
        if (queue.isEmpty() || refillQueue) {
            refillQueue()
        }
        val track: AudioTrack =
            try {
                val url = queue.poll()
                play0(url, player)
            } catch (e: LimitExceededException) {
                e.printStackTrace()
                exit()!!
            }
        currentTrack = track
        connectIfNotConnected(player)
        return track
    }

    private suspend fun play0(url: String, player: AudioPlayer) = lavaplayerManager.playTrack(url, player)

    @OptIn(KordVoice::class)
    private suspend fun connectIfNotConnected(player: AudioPlayer) {
        val state = channel.kord.getSelf(EntitySupplyStrategy.cacheWithCachingRestFallback).asMember(channel.guildId)
            .getVoiceStateOrNull()
        if (state?.channelId == null) {
            connect(player)
        }
    }

    private suspend fun refillQueue() {
        logger.debug("refilling queue...")
        queue.addAll(service.getUrls("christmas").shuffled())
        logger.debug("queue was refilled to ${queue.size} items")
    }
}
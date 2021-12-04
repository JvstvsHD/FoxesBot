package de.jvstvshd.foxesbot.module.christmas

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
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

    init {
        AudioSourceManagers.registerRemoteSources(lavaplayerManager)
    }

    private val queue = LimitedDeque<String>(limitation)

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun play(url: String): AudioTrack =
        playNext(configurePlayer(), true)

    private fun configurePlayer(): AudioPlayer {
        val player = lavaplayerManager.createPlayer()
        player.addListener { event ->
            if (event is TrackEndEvent) {
                runBlocking {
                    try {
                        if (queue.limitation.shouldLimit()) {
                            exit()
                            return@runBlocking
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

    override suspend fun exit0() {
        module.christmasTimes.remove(channel.guildId)
        lavaplayerManager.shutdown()
        if (channel is StageChannel) {
            channel.getStageInstance().delete("Beendet.")
        }
    }

    private suspend fun playNext(player: AudioPlayer, refillQueue: Boolean = false): AudioTrack {
        if (queue.isEmpty() || refillQueue) {
            refillQueue()
        }
        val track: AudioTrack
        try {
            track = lavaplayerManager.playTrack(queue.poll(), player)
        } catch (e: LimitExceededException) {
            exit()
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
        currentTrack = track
        connectIfNotConnected(player)
        return track
    }

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
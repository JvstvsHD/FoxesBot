package de.jvstvshd.foxesbot.module.christmas.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import de.jvstvshd.foxesbot.module.music.MusicService
import de.jvstvshd.foxesbot.module.music.player.AbstractMusicPlayer
import de.jvstvshd.foxesbot.module.music.player.musicPlayers
import de.jvstvshd.foxesbot.util.collection.LimitedDeque
import de.jvstvshd.foxesbot.util.limit.LimitExceededException
import de.jvstvshd.foxesbot.util.limit.Limitation
import de.jvstvshd.foxesbot.util.limit.UnlimitedLimitation
import dev.kord.common.annotation.KordVoice
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.entity.channel.StageChannel
import dev.kord.core.kordLogger
import dev.kord.core.supplier.EntitySupplyStrategy
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.runBlocking

open class ChristmasMusicPlayer(
    override val channel: BaseVoiceChannelBehavior,
    override val service: MusicService,
    limitation: Limitation = UnlimitedLimitation()
) : AbstractMusicPlayer(channel, service) {

    val lavaplayerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    override var currentTrack: AudioTrack? = null
    var lastPlayer: AudioPlayer? = null
    var started = false

    init {
        AudioSourceManagers.registerRemoteSources(lavaplayerManager)
    }

    val queue = LimitedDeque<String>(limitation)

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun play(url: String): AudioTrack {
        started = true
        return playNext(configurePlayer(), true)
    }

    open fun configurePlayer(): AudioPlayer {
        val player = lavaplayerManager.createPlayer()
        lastPlayer = player
        player.addListener { event ->
            if (event is TrackEndEvent) {
                runBlocking {
                    kordLogger.debug("stopping ${event.track.info.title}")
                    try {
                        kordLogger.debug("limit: ${queue.limitation.limit()}, current: ${queue.limitation}")
                        if (queue.limitation.shouldLimit()) {
                            exit()
                        } else {
                            playNext(player!!)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return player
    }

    override suspend fun provideAudio(player: AudioPlayer?): ByteArray? {
        return lastPlayer?.provide()?.data
    }

    override suspend fun exit0(player: AudioPlayer?): AudioTrack? {
        musicPlayers.remove(channel.guildId)
        lavaplayerManager.shutdown()
        if (channel is StageChannel) {
            (channel as StageChannel).getStageInstance().delete("Beendet.")
        }
        return currentTrack
    }

    suspend fun playNext(player: AudioPlayer, refillQueue: Boolean = false): AudioTrack {
        if (queue.isEmpty() || refillQueue) {
            refillQueue()
        }
        val track: AudioTrack =
            try {
                val url = queue.poll()
                play0(url, player)
            } catch (e: LimitExceededException) {
                kordLogger.error("Limit ${queue.limitation.limit()} was exceeded.")
                exit()!!
            }
        currentTrack = track
        connectIfNotConnected(player)
        return track
    }

    suspend fun play0(url: String, player: AudioPlayer) = lavaplayerManager.playTrack(url, player)

    @OptIn(KordVoice::class)
    private suspend fun connectIfNotConnected(player: AudioPlayer) {
        val state = channel.kord.getSelf(EntitySupplyStrategy.cacheWithCachingRestFallback).asMember(channel.guildId)
            .getVoiceStateOrNull()
        if (state?.channelId == null) {
            connect(player)
        }
    }

    private suspend fun refillQueue() {
        kordLogger.debug("refilling queue...")
        queue.addAll(service.getUrls("christmas").shuffled())
        kordLogger.debug("queue was refilled to ${queue.size} items")
    }
}
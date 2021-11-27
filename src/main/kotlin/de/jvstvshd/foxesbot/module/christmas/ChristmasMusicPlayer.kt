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
import de.jvstvshd.foxesbot.util.limit.Limitation
import de.jvstvshd.foxesbot.util.limit.UnlimitedLimitation
import dev.kord.common.annotation.KordVoice
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.supplier.EntitySupplyStrategy
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.runBlocking

class ChristmasMusicPlayer(
    override val channel: BaseVoiceChannelBehavior,
    override val service: MusicService,
    private val module: ChristmasModule,
    limitation: Limitation = UnlimitedLimitation()
) : AbstractMusicPlayer(channel, service) {

    private val lavaplayerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    override var currentTrack: AudioTrack? = null

    init {
        AudioSourceManagers.registerRemoteSources(lavaplayerManager)
    }

    private val queue = LimitedDeque<String>(limitation)
    private lateinit var allSongs: MutableList<String>

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun play(url: String): AudioTrack =
        playNext(configurePlayer(), true)

    private fun configurePlayer(): AudioPlayer {
        val player = lavaplayerManager.createPlayer()
        player.addListener { event ->
            if (event is TrackEndEvent) {
                runBlocking {
                    try {
                        println(queue.limitation.limit())
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
    }

    private suspend fun playNext(player: AudioPlayer, refillQueue: Boolean = false): AudioTrack {
        if (queue.isEmpty() || refillQueue) {
            refillQueue()
        }
        val track = lavaplayerManager.playTrack(queue.poll(), player)
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
        println("refilling queue...")
        queue.addAll(service.getUrls("christmas").shuffled())
        println("queue was refilled to ${queue.size} items")
    }
}
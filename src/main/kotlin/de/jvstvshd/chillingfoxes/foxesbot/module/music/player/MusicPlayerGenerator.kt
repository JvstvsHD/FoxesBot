package de.jvstvshd.chillingfoxes.foxesbot.module.music.player

import de.jvstvshd.chillingfoxes.foxesbot.module.music.MusicService
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior

abstract class MusicPlayerGenerator<T : MusicPlayer>(val channel: BaseVoiceChannelBehavior, val service: MusicService) {
    abstract fun generate(): T

    fun createMusicPlayer(): T {
        val currentPlayer = musicPlayers[channel.guildId] ?: return generateAndRegister()
        if (!checkInstance(currentPlayer)) {
            return generateAndRegister()
        }
        @Suppress("UNCHECKED_CAST")
        return currentPlayer as T
    }

    abstract fun checkInstance(currentPlayer: MusicPlayer): Boolean

    private fun generateAndRegister(): T = generate().also {
        musicPlayers[channel.guildId] = it
        it.init()
    }
}
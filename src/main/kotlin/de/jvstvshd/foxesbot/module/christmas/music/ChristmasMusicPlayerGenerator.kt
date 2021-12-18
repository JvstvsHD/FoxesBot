package de.jvstvshd.foxesbot.module.christmas.music

import de.jvstvshd.foxesbot.module.music.MusicService
import de.jvstvshd.foxesbot.module.music.player.MusicPlayer
import de.jvstvshd.foxesbot.module.music.player.MusicPlayerGenerator
import de.jvstvshd.foxesbot.util.limit.Limitation
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior

abstract class BaseChristmasMusicPlayerGenerator<T : ChristmasMusicPlayer>(
    channel: BaseVoiceChannelBehavior,
    service: MusicService,
    val limitation: Limitation
) : MusicPlayerGenerator<T>(channel, service)

class ChristmasMusicPlayerGenerator(channel: BaseVoiceChannelBehavior, service: MusicService, limitation: Limitation) :
    BaseChristmasMusicPlayerGenerator<ChristmasMusicPlayer>(channel, service, limitation) {

    override fun generate(): ChristmasMusicPlayer {
        return ChristmasMusicPlayer(channel, service, limitation)
    }

    override fun checkInstance(currentPlayer: MusicPlayer): Boolean = currentPlayer is ChristmasMusicPlayer
}

class ChristmasTimePlayerGenerator(channel: BaseVoiceChannelBehavior, service: MusicService, limitation: Limitation) :
    BaseChristmasMusicPlayerGenerator<ChristmasTimePlayer>(channel, service, limitation) {
    override fun generate(): ChristmasTimePlayer = ChristmasTimePlayer(channel, service, limitation)

    override fun checkInstance(currentPlayer: MusicPlayer): Boolean = currentPlayer is ChristmasTimePlayer
}
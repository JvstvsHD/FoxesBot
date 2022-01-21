package de.jvstvshd.chillingfoxes.foxesbot.module.music

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.zaxxer.hikari.HikariDataSource
import de.jvstvshd.chillingfoxes.foxesbot.module.music.commands.musicCommands
import de.jvstvshd.chillingfoxes.foxesbot.module.music.player.musicPlayers
import dev.kord.core.entity.channel.StageChannel
import kotlinx.coroutines.runBlocking

class MusicModule(val dataSource: HikariDataSource) : Extension() {

    val service = MusicService(dataSource)

    override val name = "music"

    override suspend fun setup() {
        musicCommands()
        Runtime.getRuntime().addShutdownHook(Thread {
            for (player in musicPlayers) {
                val channel = player.value.channel
                runBlocking {
                    player.value.exit()
                    if (channel is StageChannel) {
                        channel.getStageInstanceOrNull()?.delete("Delete on exit")
                    }
                }
            }
        })
    }
}
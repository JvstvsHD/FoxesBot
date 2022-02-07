package de.jvstvshd.chillingfoxes.foxesbot.module.core

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.zaxxer.hikari.HikariDataSource
import de.jvstvshd.chillingfoxes.foxesbot.config.Config
import de.jvstvshd.chillingfoxes.foxesbot.module.core.commands.commands
import de.jvstvshd.chillingfoxes.foxesbot.module.core.music.MusicService
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.rest.Image
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList

class CoreModule(val config: Config, val dataSource: HikariDataSource) : Extension() {

    override val name = "core"
    override val bundle = "core"
    val service = MusicService(dataSource)

    override suspend fun setup() {
        commands()
        event<ReadyEvent> {
            action {
                println(kord.guilds.count())
                for (guild in kord.guilds.toList()) {
                    println(guild.iconHash)
                    println("guild logo (${guild.name}): ${guild.getIconUrl(Image.Format.JPEG)}")
                }
                kord.guilds.onEach {
                    println(it.name)
                    println("guild logo (${it.name}): ${it.data.icon}")
                }
            }
        }
    }
}
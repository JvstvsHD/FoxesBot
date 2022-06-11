package de.jvstvshd.chillingfoxes.foxesbot.module.core

import com.kotlindiscord.kord.extensions.extensions.Extension
import de.jvstvshd.chillingfoxes.foxesbot.config.Config
import de.jvstvshd.chillingfoxes.foxesbot.module.core.commands.commands
import de.jvstvshd.chillingfoxes.foxesbot.module.core.music.MusicService

class CoreModule(val config: Config) : Extension() {

    override val name = "core"
    override val bundle = "core"
    val service = MusicService()

    override suspend fun setup() {
        commands()
    }
}
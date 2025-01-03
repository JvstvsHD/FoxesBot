/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.core

import de.jvstvshd.chillingfoxes.foxesbot.config.Config
import de.jvstvshd.chillingfoxes.foxesbot.module.core.commands.commands
import de.jvstvshd.chillingfoxes.foxesbot.module.core.music.MusicService
import de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.channel.createMessageEvent
import dev.kordex.core.extensions.Extension

class CoreModule(val config: Config) : Extension() {

    override val name = "core"
    val service = MusicService()

    override suspend fun setup() {
        commands()
        createMessageEvent()
    }
}
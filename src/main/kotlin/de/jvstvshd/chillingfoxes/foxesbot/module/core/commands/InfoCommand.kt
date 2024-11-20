/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.core.commands

import de.jvstvshd.chillingfoxes.foxesbot.module.core.CoreModule
import de.jvstvshd.chillingfoxes.foxesbot.util.selfAuthor
import dev.kord.rest.builder.message.embed
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.i18n.types.Key

suspend fun CoreModule.infoCommand() = publicSlashCommand {
    name = Key("info")
    description = Key("Informationen über den Bot")
    action {
        respond {
            embed {
                title = "FoxesBot"
                url = "https://discord.gg/K5rhddJtyW"
                selfAuthor()
                description =
                    "Dieser Bot dient als Bot für den Chilling Foxes Discord und wird hauptsächlich als Event Bot genutzt. Für Informationen über die Commands, benutzt /help. Weitere Hilfe gibt's im Support"
            }
        }
    }
}
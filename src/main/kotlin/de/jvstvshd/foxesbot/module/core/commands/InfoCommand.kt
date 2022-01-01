package de.jvstvshd.foxesbot.module.core.commands

import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.jvstvshd.foxesbot.module.core.CoreModule
import de.jvstvshd.foxesbot.util.KordUtil
import dev.kord.rest.builder.message.create.embed

suspend fun CoreModule.infoCommand() = publicSlashCommand {
    name = "info"
    description = "Informationen über den Bot"
    action {
        respond {
            embed {
                title = "FoxesBot"
                url = "https://discord.gg/K5rhddJtyW"
                author = KordUtil.createAuthor("JvstvsHD", "https://github.com/JvstvsHD", "https://avatars.githubusercontent.com/u/79066214?v=4")
                description = "Dieser Bot dient als Bot für den Chilling Foxes Discord und wird hauptsächlich als Event Bot genutzt. Für Infos über die Commands, benutzt /help. Weitere Hilfe gibt's im Support"
            }
        }
    }
}
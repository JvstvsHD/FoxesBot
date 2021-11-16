package de.jvstvshd.foxesbot.module.core.commands

import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.jvstvshd.foxesbot.module.core.CoreModule
import de.jvstvshd.foxesbot.utils.KordUtil
import dev.kord.rest.builder.message.create.embed

suspend fun CoreModule.infoCommand() = publicSlashCommand {
    name = "info"
    description = "Displays information of the bot"
    guild(773318688300466187L)
    action {
        respond {
            embed {
                title = "FoxesBot"
                url = "https://discord.gg/K5rhddJtyW"
                author = KordUtil.createAuthor("JvstvsHD", "https://github.com/JvstvsHD", "https://avatars.githubusercontent.com/u/79066214?v=4")
                field {
                    name = "info"
                    value = translate("commands.info.information", bundleName = "strings_de_DE.properties")
                }
            }
        }
    }
}
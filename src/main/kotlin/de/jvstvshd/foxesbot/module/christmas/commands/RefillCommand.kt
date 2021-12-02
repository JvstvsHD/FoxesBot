package de.jvstvshd.foxesbot.module.christmas.commands

import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.jvstvshd.foxesbot.module.christmas.ChristmasModule

suspend fun ChristmasModule.refillCommand() = ephemeralSlashCommand {
    name = "refill"
    description = "Refill"
    action {
        val result = refill0(guild!!.asGuild())
        if (result.exception != null) {
            respond {
                content = result.exception.userMessage
            }
            return@action
        }

        respond {
            content = ":snowflake:"
        }
    }
}
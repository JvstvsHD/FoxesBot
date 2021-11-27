package de.jvstvshd.foxesbot.module.christmas.commands

import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.jvstvshd.foxesbot.module.christmas.ChristmasModule
import de.jvstvshd.foxesbot.util.KordUtil.toLong
import java.time.LocalDateTime

suspend fun ChristmasModule.refillCommand() = publicSlashCommand {
    name = "refill"
    description = "Refill"
    action {
        val lastTime = lastTime(guild!!.toLong(), "refill")
        if ((lastTime != null) && lastTime.isBefore(LocalDateTime.now().plusDays(1).withHour(6))) {
            respond {
                content = "Bitte warte bis zum nächsten Tag!"
            }
            return@action
        }
        refill()
        respond {
            content = ":snowflake:"
        }
    }
}
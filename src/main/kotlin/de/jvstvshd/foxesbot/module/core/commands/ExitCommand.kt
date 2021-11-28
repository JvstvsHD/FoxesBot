package de.jvstvshd.foxesbot.module.core.commands

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.jvstvshd.foxesbot.module.core.CoreModule
import dev.kord.common.entity.Permission
import kotlin.system.exitProcess

suspend fun CoreModule.exitCommand() = publicSlashCommand {
    name = "exit"
    description = "Exits the bot."
    check {
        hasPermission(Permission.ManageGuild)
    }
    val kord = kord
    action {
        respond {
            content = translate("commands.exit.message")
        }
        println("Shutdown initiated")
        kord.shutdown()
        exitProcess(0)
    }
}
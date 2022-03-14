package de.jvstvshd.chillingfoxes.foxesbot.module.core.commands

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.jvstvshd.chillingfoxes.foxesbot.module.core.CoreModule
import de.jvstvshd.chillingfoxes.foxesbot.util.ShutdownTask
import dev.kord.common.entity.Permission
import dev.kord.core.kordLogger
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread
import kotlin.system.exitProcess

suspend fun CoreModule.exitCommand() = publicSlashCommand {
    name = "exit"
    description = "Exits the bot."
    check {
        hasPermission(Permission.ManageGuild)
    }
    action {
        try {
            respond {
                content = translate("commands.exit.message")
            }
            kordLogger.debug("Shutdown initiated")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        for (extension in bot.extensions.values) {
            if (extension is ShutdownTask) {
                extension.onShutdown()
            }
        }
        this@exitCommand.kord.shutdown()
        exitProcess(0)
    }
}

suspend fun CoreModule.restartCommand() = publicSlashCommand {
    name = "restart"
    description = "Startet den Bot neu"
    check {
        hasPermission(Permission.ManageGuild)
    }
    action {
        respond {
            content = "Der Bot startet neu... Dies kann einen Moment dauern."
        }
        kordLogger.debug("Restart initiated")
        this@restartCommand.kord.shutdown()
        Runtime.getRuntime().addShutdownHook(thread(start = false, isDaemon = true, name = "Foxes Bot Restart Thread") {
            if (System.getProperty("os.name").lowercase().contains("win")) {
                runBlocking {
                    respond {
                        content = "OS: Windows: Bot kann nicht neugestartet werden."
                    }
                }
                kordLogger.warn("Cannot restart bot on windows.")
                exitProcess(0)
            }
            Runtime.getRuntime().exec("sh start.sh")
        })
        exitProcess(0)
    }
}
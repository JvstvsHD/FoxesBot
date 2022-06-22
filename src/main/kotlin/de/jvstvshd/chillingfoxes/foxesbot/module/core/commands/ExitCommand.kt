/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.core.commands

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.jvstvshd.chillingfoxes.foxesbot.module.core.CoreModule
import de.jvstvshd.chillingfoxes.foxesbot.util.ShutdownTask
import dev.kord.common.entity.Permission
import dev.kord.core.kordLogger
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.time.DurationFormatUtils
import kotlin.concurrent.thread
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
suspend fun CoreModule.exitCommand() = publicSlashCommand {
    name = "exit"
    description = "Exits the bot."
    check {
        hasPermission(Permission.ManageGuild)
    }
    action {
        val duration = measureTime {
            for (extension in bot.extensions.values) {
                if (extension is ShutdownTask) {
                    try {
                        extension.onShutdown()
                    } catch (e: Exception) {
                        kordLogger.error("Execution for shutdown task ${extension.javaClass.name} failed: $e")
                    }
                }
            }
        }
        try {
            respond {
                content = "Der Bot fährt nun herunter. Zeit zum Ausführen der Shutdown-Tasks: ${
                    DurationFormatUtils.formatDurationHMS(duration.inWholeMilliseconds)
                }"
            }
            kordLogger.debug("Shutdown tasks took ${duration.inWholeMilliseconds}ms to finish.")
            kordLogger.debug("Shutdown initiated")
        } catch (e: Exception) {
            e.printStackTrace()
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
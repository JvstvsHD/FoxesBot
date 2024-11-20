/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.core.commands

import de.jvstvshd.chillingfoxes.foxesbot.logger
import de.jvstvshd.chillingfoxes.foxesbot.module.core.CoreModule
import de.jvstvshd.chillingfoxes.foxesbot.util.ShutdownTask
import de.jvstvshd.chillingfoxes.foxesbot.util.isPermitted
import dev.kord.common.entity.Permission
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.i18n.types.Key
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.time.DurationFormatUtils
import kotlin.concurrent.thread
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
suspend fun CoreModule.exitCommand() = publicSlashCommand {
    name = Key("exit")
    description = Key("Exits the bot.")
    check {
        isPermitted(Permission.ManageGuild)
    }
    action {
        val duration = measureTime {
            for (extension in bot.extensions.values) {
                if (extension is ShutdownTask) {
                    try {
                        extension.onShutdown()
                    } catch (e: Exception) {
                        logger.error(e) { "Execution for shutdown task ${extension.javaClass.name} failed" }
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
            logger.debug { "Shutdown tasks took ${duration.inWholeMilliseconds}ms to finish." }
            logger.debug { "Shutdown initiated" }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        bot.stop()
        this@exitCommand.kord.shutdown()
        exitProcess(0)
    }
}

suspend fun CoreModule.restartCommand() = publicSlashCommand {
    name = Key("restart")
    description = Key("Startet den Bot neu")
    check {
        isPermitted(Permission.ManageGuild)
    }
    action {
        respond {
            content = "Der Bot startet neu... Dies kann einen Moment dauern."
        }
        logger.debug { "Restart initiated" }
        this@restartCommand.kord.shutdown()
        Runtime.getRuntime().addShutdownHook(thread(start = false, isDaemon = true, name = "Foxes Bot Restart Thread") {
            if (System.getProperty("os.name").lowercase().contains("win")) {
                runBlocking {
                    respond {
                        content = "OS: Windows: Bot kann nicht neugestartet werden."
                    }
                }
                logger.warn { "Cannot restart bot on windows." }
                exitProcess(0)
            }
            Runtime.getRuntime().exec("sh start.sh")
        })
        exitProcess(0)
    }
}
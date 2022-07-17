/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.util

import com.kotlindiscord.kord.extensions.*
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import de.jvstvshd.chillingfoxes.foxesbot.config.data.ConfigData
import dev.kord.core.Kord
import dev.kord.core.behavior.execute
import dev.kord.core.entity.Webhook
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.WebhookMessageCreateBuilder
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Instant
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.*
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.Property
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.config.plugins.PluginAttribute
import org.apache.logging.log4j.core.config.plugins.PluginElement
import org.apache.logging.log4j.core.config.plugins.PluginFactory
import org.apache.logging.log4j.core.impl.MutableLogEvent
import org.apache.logging.log4j.message.SimpleMessage
import org.koin.core.error.NoBeanDefFoundException
import java.io.Serializable

@Plugin(name = "DiscordWebhookAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
class DiscordWebhookAppender(
    name: String,
    filter: Filter?,
    layout: Layout<out Serializable>?,
    ignoreExceptions: Boolean,
    properties: Array<out Property>?
) : AbstractAppender(name, filter, layout, ignoreExceptions, properties), KordExKoinComponent {

    private val logEvents = mutableListOf<LogEvent>()
    private val mutex = Mutex()

    companion object {
        @Suppress("unused")
        @JvmStatic
        @PluginFactory
        fun createAppender(
            @PluginAttribute("name") name: String,
            @PluginElement("Filter") filter: Filter?
        ): DiscordWebhookAppender = DiscordWebhookAppender(name, filter, null, false, null)
    }

    override fun append(event: LogEvent) {
        // sending debug logs to the webhook would cause some recursion (only some....)
        if (event.level == Level.DEBUG || event.level == Level.OFF) {
            return
        }
        if (event.message.formattedMessage.contains("\\")) {
            println(event.message.formattedMessage)
            (event as MutableLogEvent).let {
                it.message = SimpleMessage("no plugins root")
            }
            println(event.message.formattedMessage)
        }
        logEvents.add(event)
        try {
            getKoin()
        } catch (e: java.lang.IllegalStateException) {
            return
        }
        val kord: Kord
        val configData: ConfigData
        try {
            kord = getKoin().get()
        } catch (exception: NoBeanDefFoundException) {
            return
        }
        try {
            configData = getKoin().get()
        } catch (e: NoBeanDefFoundException) {
            return
        }
        try {
            runBlocking {
                val webhookUrl = configData.baseData.loggingWebhook ?: return@runBlocking
                val (idString, token) = webhookUrl.split("/")
                val id = idString.toLong().snowflake
                val webhook = kord.getWebhookWithToken(id, token)
                send(webhook, token)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun send(webhook: Webhook, token: String) {
        mutex.withLock {
            val newList = ArrayList(logEvents.filter { logEvent -> logEvent.level != Level.OFF })
            logEvents.clear()
            for (logEvent in newList) {
                webhook.execute(token) {
                    createWebhookMessageCreateBuilder(logEvent)
                }
            }
        }
    }

    private fun WebhookMessageCreateBuilder.createWebhookMessageCreateBuilder(event: LogEvent) {
        embed {
            colorLevelBased(event.level)
            title = event.level.toString()
            description = event.message.formattedMessage
            event.thrown?.let {
                field {
                    name = StringUtils.abbreviate(ExceptionUtils.getMessage(event.thrown), 256)
                    value = StringUtils.abbreviate(ExceptionUtils.getStackTrace(event.thrown), 1024)
                }
            }
            timestamp = Instant.fromEpochMilliseconds(event.instant.epochMillisecond)
        }
    }

    private fun EmbedBuilder.colorLevelBased(level: Level) {
        color = when (level) {
            Level.DEBUG -> DISCORD_GREEN
            Level.INFO -> DISCORD_BLURPLE
            Level.WARN -> DISCORD_YELLOW
            Level.ERROR, Level.FATAL -> DISCORD_RED
            else -> DISCORD_WHITE
        }
    }
}
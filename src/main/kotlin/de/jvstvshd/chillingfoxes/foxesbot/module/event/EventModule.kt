/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.event

import de.jvstvshd.chillingfoxes.foxesbot.config.Config
import de.jvstvshd.chillingfoxes.foxesbot.io.EventData
import de.jvstvshd.chillingfoxes.foxesbot.io.EventDataTable
import de.jvstvshd.chillingfoxes.foxesbot.logger
import de.jvstvshd.chillingfoxes.foxesbot.module.event.commands.countdownEventResetStateCommand
import de.jvstvshd.chillingfoxes.foxesbot.module.event.commands.countdownStartCommand
import de.jvstvshd.chillingfoxes.foxesbot.util.ShutdownTask
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.MessageDeleteEvent
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.event
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

const val COUNTDOWN_EVENT_NAME = "countdown_event"
val countdownEvents = mutableListOf<CountdownEvent>()

class EventModule(val config: Config) : Extension(), ShutdownTask {
    override val name: String = "event"

    override suspend fun setup() {
        loadCountdownEvents()
        countdownStartCommand()
        countdownEventResetStateCommand()
        event<MessageCreateEvent> {
            action {
                if (event.member?.isBot == true) {
                    return@action
                }
                countdownEvents.firstOrNull { countdownEvent -> countdownEvent.data.channel.id == event.message.channelId }
                    ?.countdown(event.message)
            }
        }
        event<MessageDeleteEvent> {
            action {
                if (countdownEvents.any { countdownEvent -> countdownEvent.data.channel.id == event.channelId }) {
                    if (!allowedDeletedMessages.contains(event.messageId)) {
                        event.channel.createMessage("Die Nachricht von ${event.message?.author?.mention} wurde durch einen User gelöscht.\nInhalt: ${event.message?.content}")
                    }
                }
            }
        }
    }

    private fun loadCountdownEvents() =
        kord.launch {
            newSuspendedTransaction {
                EventData.find { EventDataTable.type eq COUNTDOWN_EVENT_NAME }.forEach {
                    val decodedData: CountdownEventData =
                        runCatching { Json.decodeFromString<CountdownEventData>(it.data) }.onFailure { exception -> logger.warn { "Could not load event: " + exception.message } }
                            .getOrNull() ?: return@forEach
                    countdownEvents.add(
                        CountdownEvent(
                            decodedData,
                            config.configData,
                            kord
                        )
                    )
                }
            }
            logger.info("loaded ${countdownEvents.size} countdown events from database")
            for ((index, countdownEvent) in countdownEvents.withIndex()) {
                logger.info("CD Event #$index: ${countdownEvent.data.channel.asChannel().name} in guild ${countdownEvent.data.channel.guild.asGuild().name}")
                countdownEvent.unlock()
            }
        }

    override suspend fun unload() {
        println("unload")
        for (countdownEvent in countdownEvents) {
            countdownEvent.save()
        }
    }

    override suspend fun onShutdown() {
        for (countdownEvent in countdownEvents) {
            countdownEvent.save()
        }
    }
}
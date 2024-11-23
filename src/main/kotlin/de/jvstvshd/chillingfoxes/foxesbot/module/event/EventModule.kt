/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.event

import de.jvstvshd.chillingfoxes.foxesbot.config.Config
import de.jvstvshd.chillingfoxes.foxesbot.io.EventType
import de.jvstvshd.chillingfoxes.foxesbot.module.event.commands.eventCommand
import de.jvstvshd.chillingfoxes.foxesbot.module.event.countdown.CountdownEvent
import de.jvstvshd.chillingfoxes.foxesbot.module.event.countdown.allowedDeletedMessages
import de.jvstvshd.chillingfoxes.foxesbot.module.event.countdown.countdownEventResetStateCommand
import de.jvstvshd.chillingfoxes.foxesbot.module.event.countdown.countdownStartCommand
import de.jvstvshd.chillingfoxes.foxesbot.util.ShutdownTask
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.MessageDeleteEvent
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.event
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

const val COUNTDOWN_EVENT_NAME = "countdown_event"
val countdownEvents = mutableListOf<CountdownEvent>()

class EventModule(val config: Config) : Extension(), ShutdownTask {
    override val name: String = "event"
    val registeredTypes: MutableSet<String> = mutableSetOf()

    override suspend fun setup() {
        //loadCountdownEvents()
        eventCommand()
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
        newSuspendedTransaction {
            EventType.all().forEach { eventType ->
                registeredTypes.add(eventType.name)
            }
        }
    }

    /*private fun loadCountdownEvents() =
        kord.launch {
            newSuspendedTransaction {
                EventData.find { EventOccurrence.type eq Events.COUNTDOWN }.forEach {
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
            logger.info { "loaded ${countdownEvents.size} countdown events from database" }
            for ((index, countdownEvent) in countdownEvents.withIndex()) {
                logger.info { "CD Event #$index: ${countdownEvent.data.channel.id} in guild ${countdownEvent.data.channel.guild.id}" }
                countdownEvent.unlock()
            }
        }*/

    override suspend fun unload() {
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
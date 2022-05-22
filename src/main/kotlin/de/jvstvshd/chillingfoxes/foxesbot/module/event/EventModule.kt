package de.jvstvshd.chillingfoxes.foxesbot.module.event

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.zaxxer.hikari.HikariDataSource
import de.jvstvshd.chillingfoxes.foxesbot.config.Config
import de.jvstvshd.chillingfoxes.foxesbot.io.EventData
import de.jvstvshd.chillingfoxes.foxesbot.io.EventDataTable
import de.jvstvshd.chillingfoxes.foxesbot.module.event.commands.countdownEventResetStateCommand
import de.jvstvshd.chillingfoxes.foxesbot.module.event.commands.countdownStartCommand
import de.jvstvshd.chillingfoxes.foxesbot.util.ShutdownTask
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.MessageDeleteEvent
import dev.kord.core.kordLogger
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

const val COUNTDOWN_EVENT_NAME = "countdown_event"
val countdownEvents = mutableListOf<CountdownEvent>()

class EventModule(
    @Deprecated(message = "Use API Exposed instead.") val dataSource: HikariDataSource,
    val config: Config
) : Extension(), ShutdownTask {
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
                    countdownEvents.add(
                        CountdownEvent(
                            Json.decodeFromString(it.data),
                            config.configData,
                            kord
                        )
                    )
                }
            }
            kordLogger.info("loaded ${countdownEvents.size} countdown events from database")
            for ((index, countdownEvent) in countdownEvents.withIndex()) {
                kordLogger.info("CD Event #$index: ${countdownEvent.data.channel.asChannel().name} in guild ${countdownEvent.data.channel.guild.asGuild().name}")
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
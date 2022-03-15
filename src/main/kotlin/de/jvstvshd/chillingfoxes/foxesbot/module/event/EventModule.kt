package de.jvstvshd.chillingfoxes.foxesbot.module.event

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.zaxxer.hikari.HikariDataSource
import de.jvstvshd.chillingfoxes.foxesbot.config.Config
import de.jvstvshd.chillingfoxes.foxesbot.module.event.commands.countdownEventResetStateCommand
import de.jvstvshd.chillingfoxes.foxesbot.module.event.commands.countdownStartCommand
import de.jvstvshd.chillingfoxes.foxesbot.util.ShutdownTask
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.kordLogger
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

const val COUNTDOWN_EVENT_NAME = "countdown_event"
val countdownEvents = mutableListOf<CountdownEvent>()


class EventModule(val dataSource: HikariDataSource, val config: Config) : Extension(), ShutdownTask {
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
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun loadCountdownEvents() {
        GlobalScope.launch {
            dataSource.connection.use { connection ->
                connection.prepareStatement("SELECT guild_id, channel_id, data FROM event_data WHERE type = ?")
                    .use { statement ->
                        statement.setString(1, COUNTDOWN_EVENT_NAME)
                        val rs = statement.executeQuery()
                        synchronized(countdownEvents) {
                            while (rs.next()) {
                                countdownEvents.add(
                                    CountdownEvent(
                                        Json.decodeFromString(rs.getString(3)),
                                        config.configData,
                                        dataSource,
                                        kord
                                    )
                                )
                            }
                        }
                    }
            }
            kordLogger.info("loaded ${countdownEvents.size} countdown events from database")
            for ((index, countdownEvent) in countdownEvents.withIndex()) {
                kordLogger.info("CD Event #$index: ${countdownEvent.data.channel.asChannel().name} in guild ${countdownEvent.data.channel.guild.asGuild().name}")
                countdownEvent.unlock()
            }
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
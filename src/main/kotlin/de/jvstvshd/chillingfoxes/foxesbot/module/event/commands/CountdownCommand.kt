package de.jvstvshd.chillingfoxes.foxesbot.module.event.commands

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.commands.converters.impl.long
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.jvstvshd.chillingfoxes.foxesbot.module.event.*
import de.jvstvshd.chillingfoxes.foxesbot.util.KordUtil.toLong
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.TextChannelBehavior
import java.time.LocalDateTime

class CountdownArgs : Arguments() {
    val startValue by long {
        name = "start"
        description = "Startwert des Countdowns"
        validate {
            if (value <= 0) {
                fail("Der Startwert muss > 0 sein.")
                return@validate
            }
            pass()
        }
    }
    val channel by channel {
        name = "channel"
        description = "Channel, in dem das Event stattfinden soll"
        validate {
            println(value::class.java)
            println(value.type)
            if (value.type != ChannelType.GuildText) {
                fail("Der angegebene Channel ist kein Text-Channel!")
                return@validate
            }
            /*if (value is NewsChannelBehavior) {
                fail("News Channel sind nicht für Countdown-Events vorgesehen!")
                return@validate
            }
            if (value is ThreadChannelBehavior) {
                fail("Thread Channel werden derzeit nicht unterstützt.")
                return@validate
            }*/
            pass()
        }
    }
}

suspend fun EventModule.countdownStartCommand() = publicSlashCommand(::CountdownArgs) {
    name = "countdown"
    description = "Startet ein Countdown-Event im angegebenen Channel"
    check {
        hasPermission(Permission.ManageGuild)
    }
    action {
        val channel = guild!!.getChannel(arguments.channel.id) as TextChannelBehavior
        dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT channel_id FROM channel_barriers WHERE name = ? AND guild_id = ?")
                .use { preparedStatement ->
                    preparedStatement.setString(1, COUNTDOWN_EVENT_NAME)
                    preparedStatement.setLong(2, guild!!.toLong())
                    val resultSet = preparedStatement.executeQuery()
                    if (!resultSet.next()) {
                        respond {
                            content = "Es existiert kein zulässiger Channel!"
                        }
                        return@action
                    }
                    val channelId = resultSet.getLong(1)
                    if (channelId != channel.asChannel().category?.toLong()!!) {
                        respond {
                            content = "Der Channel ist nicht zulässig."
                        }
                        return@action
                    }
                }
        }
        val startValue = arguments.startValue
        val event = CountdownEvent(
            CountdownEventData(channel, startValue, mutableMapOf(), LocalDateTime.now(), member?.toLong() ?: -1),
            config.configData,
            dataSource,
            this@countdownStartCommand.kord
        )
        countdownEvents.add(event)
        event.start()
        respond {
            content = "Ein Countdown-Event, beginnend ab $startValue, wurde in ${channel.mention} gestartet."
        }
    }
}
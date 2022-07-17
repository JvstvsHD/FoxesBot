/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.event.commands

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.enumChoice
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.commands.converters.impl.long
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.jvstvshd.chillingfoxes.foxesbot.io.ChannelBarrier
import de.jvstvshd.chillingfoxes.foxesbot.io.ChannelBarriersTable
import de.jvstvshd.chillingfoxes.foxesbot.module.event.*
import de.jvstvshd.chillingfoxes.foxesbot.util.long
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.TextChannelBehavior
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
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

class CountdownEventResetArgs : Arguments() {
    val type by enumChoice<CountdownResetState> {
        name = "typ"
        typeName = "typeName"
        description = "auf was der Countdown im Falle eines Fails zurückgesetzt werden soll"
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
        val allowedChannels = newSuspendedTransaction {
            ChannelBarrier.find { (ChannelBarriersTable.name eq COUNTDOWN_EVENT_NAME) and (ChannelBarriersTable.guildId eq guild!!.long) }
                .map { it.channelId }
        }
        if (allowedChannels.isEmpty()) {
            respond {
                content = "Es existiert kein zulässiger Channel!"
            }
            return@action
        }
        println(allowedChannels.contains(channel.fetchChannel().category?.long))
        if (!(allowedChannels.contains(channel.long) || allowedChannels.contains(channel.fetchChannel().category?.long))) {
            respond {
                content = "Der Channel ist nicht zulässig."
            }
            return@action
        }
        val startValue = arguments.startValue
        val event = CountdownEvent(
            CountdownEventData(channel, startValue, mutableMapOf(), LocalDateTime.now(), member?.long ?: -1),
            config.configData,
            this@countdownStartCommand.kord
        )
        countdownEvents.add(event)
        event.start()
        respond {
            content = "Ein Countdown-Event, beginnend ab $startValue, wurde in ${channel.mention} gestartet."
        }
    }
}

suspend fun EventModule.countdownEventResetStateCommand() = publicSlashCommand(::CountdownEventResetArgs) {
    name = "countdownreset"
    description = "Setzt die Art des Wertes, auf den der Countdown im Falle eines Fails zurückgesetzt werden soll"
    check {
        hasPermission(Permission.ManageGuild)
    }
    action {
        config.configData.eventData.countdownResetState = arguments.type
        config.save()
        respond {
            content = "Der Reset Typ wurde erfolgreich auf ${arguments.type.readableName} gesetzt!"
        }
    }
}
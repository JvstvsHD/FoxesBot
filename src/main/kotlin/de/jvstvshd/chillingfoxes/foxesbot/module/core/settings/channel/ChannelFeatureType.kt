/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.channel

import com.kotlindiscord.kord.extensions.utils.hasPermission
import de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.EntityFeatureData
import de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.EntityFeatureType
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.channel.GuildChannelBehavior
import dev.kord.core.behavior.channel.TopGuildChannelBehavior
import dev.kord.core.behavior.channel.editMemberPermission
import dev.kord.core.event.Event
import dev.kord.core.event.message.MessageCreateEvent

sealed class ChannelFeatureType<T : EntityFeatureData<GuildChannelBehavior>>(override val id: String) :
    EntityFeatureType<T, GuildChannelBehavior> {

    companion object {
        val features = listOf(Barrier, OneMessage, SuppressPresenceCheck)

        fun fromString(name: String): ChannelFeatureType<out ChannelFeatureData>? =
            features.find { it.id == name.lowercase() }

        fun fromStringOrElseThrow(name: String) =
            fromString(name) ?: throw IllegalArgumentException("unknown channel feature type $name")
    }

    open suspend fun handle(data: T) {
        throw UnsupportedOperationException()
    }

    abstract suspend fun createData(event: Event): T?

    protected inline fun <reified T : Event> asEvent(event: Event) = if (event is T) event else null

    object Barrier : ChannelFeatureType<ChannelFeatureData>("channel_barrier") {

        override val name: String
            get() = "ChannelBarrier"
        override val description: String
            get() = "Aktionen, die nur in einem Channel (einer Kategorie) ausgeführt werden sollen"

        override suspend fun handle(data: ChannelFeatureData) {}

        override suspend fun createData(event: Event): ChannelFeatureData? {
            val e = asEvent<MessageCreateEvent>(event) ?: return null
            val channel = e.message.channel
            if (channel !is GuildChannelBehavior) return null
            return ChannelFeatureData(channel)
        }
    }

    object OneMessage : ChannelFeatureType<MessageChannelFeatureData>("one_message") {

        override val name: String
            get() = "OneMessage"
        override val description: String
            get() = "Es kann nur eine Nachricht pro User gesendet werden"

        override suspend fun handle(data: MessageChannelFeatureData) {
            val channel = data.entity
            val message = data.message.asMessage()
            val author = message.getAuthorAsMember() ?: return
            if (author.hasPermission(Permission.Administrator) || author.isBot) {
                return
            }
            channel.editMemberPermission(author.id) {
                denied = Permissions(Permission.SendMessages)
            }
        }

        override suspend fun createData(event: Event): MessageChannelFeatureData? {
            val e = asEvent<MessageCreateEvent>(event) ?: return null
            val message = e.message
            if (message.channel !is TopGuildChannelBehavior) return null
            return MessageChannelFeatureData(message.channel as TopGuildChannelBehavior, message)
        }
    }

    object SuppressPresenceCheck : ChannelFeatureType<ChannelFeatureData>("suppress_presence_check") {

        override val name: String
            get() = "Präsenz-Status Check: Unterdrückung"
        override val description: String
            get() = "Ausnahme des Channels vom Präsenz-Status Check"

        override suspend fun createData(event: Event): ChannelFeatureData? = null
    }
}

typealias ChannelFeatureData = EntityFeatureData<GuildChannelBehavior>

fun ChannelFeatureData(channel: GuildChannelBehavior) = object : ChannelFeatureData {
    override val entity: GuildChannelBehavior
        get() = channel
}

open class MessageChannelFeatureData(override val entity: TopGuildChannelBehavior, open val message: MessageBehavior) :
    ChannelFeatureData
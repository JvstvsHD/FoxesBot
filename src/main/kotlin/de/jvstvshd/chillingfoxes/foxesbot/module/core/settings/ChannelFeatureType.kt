/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.core.settings

import com.kotlindiscord.kord.extensions.utils.hasPermission
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.behavior.channel.GuildChannelBehavior
import dev.kord.core.behavior.channel.TopGuildChannelBehavior
import dev.kord.core.behavior.channel.editMemberPermission
import dev.kord.core.event.Event
import dev.kord.core.event.message.MessageCreateEvent

sealed class ChannelFeatureType<T : ChannelFeatureData>(val name: String) {

    companion object {
        val features = listOf(Barrier, OneMessage)

        fun fromString(name: String) = features.find { it.name == name.lowercase() }

        fun fromStringOrElseThrow(name: String) =
            fromString(name) ?: throw IllegalArgumentException("unknown channel feature type $name")
    }

    open suspend fun handle(data: T) {
        throw UnsupportedOperationException()
    }

    abstract suspend fun createData(event: Event): T?

    protected inline fun <reified T : Event> asEvent(event: Event) = if (event is T) event else null

    object Barrier : ChannelFeatureType<ChannelFeatureData>("channel_barrier") {
        override suspend fun handle(data: ChannelFeatureData) {
            TODO("Not yet implemented")
        }

        override suspend fun createData(event: Event): ChannelFeatureData? {
            val e = asEvent<MessageCreateEvent>(event) ?: return null
            val channel = e.message.channel
            if (channel !is GuildChannelBehavior) return null
            return ChannelFeatureData(channel)
        }
    }

    object OneMessage : ChannelFeatureType<MessageChannelFeatureData>("one_message") {

        override suspend fun handle(data: MessageChannelFeatureData) {
            val channel = data.channel
            val message = data.message.asMessage()
            val channelFeature = ChannelFeature.feature(channel)
            if (!channelFeature.isFeatureEnabled(OneMessage)) {
                return
            }
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

        override suspend fun createData(event: Event): ChannelFeatureData? = null
    }
}

interface ChannelFeatureData {
    val channel: GuildChannelBehavior
}

fun ChannelFeatureData(channel: GuildChannelBehavior) = object : ChannelFeatureData {
    override val channel: GuildChannelBehavior
        get() = channel
}

open class MessageChannelFeatureData(override val channel: TopGuildChannelBehavior, open val message: MessageBehavior) :
    ChannelFeatureData

inline fun <reified T : ChannelBehavior> ChannelBehavior.castChannel(): T? = if (this is T) this else null
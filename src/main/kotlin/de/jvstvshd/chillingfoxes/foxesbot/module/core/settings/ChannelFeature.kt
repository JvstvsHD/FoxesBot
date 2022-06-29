/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.core.settings

import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.getKoin
import com.kotlindiscord.kord.extensions.utils.hasPermission
import de.jvstvshd.chillingfoxes.foxesbot.io.ChannelSettings
import de.jvstvshd.chillingfoxes.foxesbot.io.ChannelSettingsTable
import de.jvstvshd.chillingfoxes.foxesbot.module.core.CoreModule
import de.jvstvshd.chillingfoxes.foxesbot.util.KordUtil.long
import dev.kord.cache.api.data.description
import dev.kord.cache.api.put
import dev.kord.cache.api.query
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.GuildChannelBehavior
import dev.kord.core.behavior.channel.editMemberPermission
import dev.kord.core.entity.channel.TopGuildChannel
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.kordLogger
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ChannelFeature private constructor(
    val channel: GuildChannelBehavior,
    val features: MutableMap<ChannelFeatureType, Boolean>
) {

    companion object {

        val dataDescription = description(ChannelFeature::channel)

        suspend fun feature(channel: GuildChannelBehavior): ChannelFeature {
            val kord: Kord by getKoin().inject()
            val queryItem = kord.cache.query<ChannelFeature> { ChannelFeature::channel eq channel }.singleOrNull()
            if (queryItem != null) {
                return queryItem
            }
            val channelId = channel.long
            val guildId = channel.guild.long
            val features = mutableMapOf<ChannelFeatureType, Boolean>()
            val feature = newSuspendedTransaction {
                val results =
                    ChannelSettings.find { (ChannelSettingsTable.guildId eq guildId) and (ChannelSettingsTable.channelId eq channelId) }
                if (results.empty()) {
                    return@newSuspendedTransaction createFeature(channel)
                } else {
                    results.onEach {
                        features[ChannelFeatureType.fromStringOrElseThrow(it.type)] = it.activated
                    }
                    return@newSuspendedTransaction ChannelFeature(channel, features)
                }
            }
            kord.cache.put(feature)
            return feature
        }

        private suspend fun createFeature(channel: GuildChannelBehavior) = newSuspendedTransaction {
            for (feature in ChannelFeatureType.features) {
                ChannelSettings.new {
                    this.channelId = channel.long
                    this.guildId = channel.guild.long
                    this.type = feature.name
                    this.activated = false
                }
            }
            return@newSuspendedTransaction ChannelFeature(
                channel,
                ChannelFeatureType.features.associateWith { false }.toMutableMap()
            )
        }

        suspend fun reloadAll() {
            kordLogger.info("Reloading all channel feature entries from the database")
            kordLogger.warn("This is not intended in production but rather for debugging, if at all.")
            val kord by getKoin().inject<Kord>()
            kord.launch {
                val start = System.currentTimeMillis()
                newSuspendedTransaction {
                    val query = kord.cache.query<ChannelFeature>()
                    kordLogger.info("Reloading ${query.count()} items. This may take a while...")
                    val newData = mutableListOf<ChannelFeature>()
                    query.asFlow().collect { channelFeature ->
                        val features = mutableMapOf<ChannelFeatureType, Boolean>()
                        ChannelSettings.find { (ChannelSettingsTable.guildId eq channelFeature.channel.guild.long) and (ChannelSettingsTable.channelId eq channelFeature.channel.long) }
                            .onEach {
                                features[ChannelFeatureType.fromStringOrElseThrow(it.type)] = it.activated
                            }
                        newData.add(ChannelFeature(channelFeature.channel, features))
                    }
                }
                kordLogger.info("Reloading all channel features took ${System.currentTimeMillis() - start} ms")
            }
        }
    }

    suspend fun update() = newSuspendedTransaction {
        for ((feature, active) in features) {
            ChannelSettings.find {
                (ChannelSettingsTable.guildId eq channel.guild.long) and
                        (ChannelSettingsTable.channelId eq channel.long) and
                        (ChannelSettingsTable.type eq feature.name)
            }.onEach {
                it.activated = active
            }
        }
    }

    fun isFeatureEnabled(type: ChannelFeatureType) = features[type] == true
}

sealed class ChannelFeatureType(val name: String) {

    companion object {
        val features = listOf(Barrier, OneMessage)

        fun fromString(name: String) = features.find { it.name == name.lowercase() }

        fun fromStringOrElseThrow(name: String) =
            fromString(name) ?: throw IllegalArgumentException("unknown channel feature type $name")
    }

    object Barrier : ChannelFeatureType("channel_barrier")
    object OneMessage : ChannelFeatureType("one_message")
}

suspend fun CoreModule.createMessageEvent() = event<MessageCreateEvent> {
    action {
        val channel = event.message.channel.asChannel()
        println(channel.javaClass)
        if (channel !is TopGuildChannel) {
            return@action
        }
        val channelFeature = ChannelFeature.feature(channel)
        if (!channelFeature.isFeatureEnabled(ChannelFeatureType.OneMessage)) {
            return@action
        }
        val author = event.message.getAuthorAsMember() ?: return@action
        if (author.hasPermission(Permission.Administrator) || author.isBot) {
            return@action
        }
        channel.editMemberPermission(author.id) {
            denied = Permissions(Permission.SendMessages)
        }
    }
}
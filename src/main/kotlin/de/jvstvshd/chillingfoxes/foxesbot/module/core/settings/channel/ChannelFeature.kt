/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

@file:Suppress("UNCHECKED_CAST")

package de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.channel

import com.kotlindiscord.kord.extensions.extensions.event
import de.jvstvshd.chillingfoxes.foxesbot.io.ChannelSettings
import de.jvstvshd.chillingfoxes.foxesbot.io.ChannelSettingsTable
import de.jvstvshd.chillingfoxes.foxesbot.module.core.CoreModule
import de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.EntityFeature
import de.jvstvshd.chillingfoxes.foxesbot.util.long
import dev.kord.cache.api.DataCache
import dev.kord.cache.api.Query
import dev.kord.cache.api.data.description
import dev.kord.cache.api.put
import dev.kord.cache.api.query
import dev.kord.core.behavior.channel.GuildChannelBehavior
import dev.kord.core.event.message.MessageCreateEvent
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class ChannelFeature private constructor(
    override val entity: GuildChannelBehavior,
    override val features: MutableMap<ChannelFeatureType<out ChannelFeatureData>, Boolean>
) : EntityFeature<GuildChannelBehavior, ChannelFeatureType<out ChannelFeatureData>>(entity, features) {

    companion object :
        EntityFeatureCompanion<GuildChannelBehavior, ChannelFeatureType<out ChannelFeatureData>, ChannelFeature> {

        val dataDescription = description(ChannelFeature::entity)

        override val allFeatures: List<ChannelFeatureType<out ChannelFeatureData>>
            get() = ChannelFeatureType.features

        override suspend fun createFeature(
            member: GuildChannelBehavior,
            features: List<ChannelFeatureType<out ChannelFeatureData>>
        ): ChannelFeature = newSuspendedTransaction {
            for (feature in features) {
                ChannelSettings.new {
                    this.channelId = member.long
                    this.guildId = member.guild.long
                    this.type = feature.id
                    this.activated = false
                }
            }
            return@newSuspendedTransaction ChannelFeature(
                member,
                ChannelFeatureType.features.associateWith { false }.toMutableMap()
            )
        }

        override suspend fun DataCache.queryFromCache(entity: GuildChannelBehavior): Query<ChannelFeature> =
            query { ChannelFeature::entity eq entity }

        override suspend fun DataCache.cache(feature: ChannelFeature) {
            put(feature)
        }

        override suspend fun query(entity: GuildChannelBehavior): ChannelFeature {
            val channelId = entity.long
            val guildId = entity.guild.long
            val features = mutableMapOf<ChannelFeatureType<out ChannelFeatureData>, Boolean>()
            return newSuspendedTransaction {
                val results =
                    ChannelSettings.find { (ChannelSettingsTable.guildId eq guildId) and (ChannelSettingsTable.channelId eq channelId) }
                if (results.empty()) {
                    return@newSuspendedTransaction createFeature(entity)
                } else {
                    results.onEach {
                        features[ChannelFeatureType.fromStringOrElseThrow(it.type)] = it.activated
                    }
                    return@newSuspendedTransaction ChannelFeature(entity, features)
                }
            }
        }
    }

    override suspend fun update() = newSuspendedTransaction {
        for ((feature, active) in features) {
            ChannelSettings.find {
                (ChannelSettingsTable.guildId eq entity.guild.long) and
                        (ChannelSettingsTable.channelId eq entity.long) and
                        (ChannelSettingsTable.type eq feature.id)
            }.onEach {
                it.activated = active
            }
        }
    }
}

suspend fun CoreModule.createMessageEvent() = event<MessageCreateEvent> {
    action {
        val channel = event.message.channel
        if (channel !is GuildChannelBehavior) {
            return@action
        }
        val channelFeature = ChannelFeature.feature(channel)
        for ((feature, active) in channelFeature.features) {
            if (!active) continue
            feature.createData(event)?.let {
                (feature as ChannelFeatureType<ChannelFeatureData>).handle(it)
            }
        }
    }
}
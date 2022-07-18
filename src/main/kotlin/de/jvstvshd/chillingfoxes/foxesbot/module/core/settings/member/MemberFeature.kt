/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.member

import de.jvstvshd.chillingfoxes.foxesbot.io.ChannelSettings
import de.jvstvshd.chillingfoxes.foxesbot.io.ChannelSettingsTable
import de.jvstvshd.chillingfoxes.foxesbot.io.MemberSettings
import de.jvstvshd.chillingfoxes.foxesbot.io.MemberSettingsTable
import de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.EntityFeature
import de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.EntityFeatureData
import de.jvstvshd.chillingfoxes.foxesbot.util.long
import dev.kord.cache.api.DataCache
import dev.kord.cache.api.Query
import dev.kord.cache.api.data.description
import dev.kord.cache.api.put
import dev.kord.cache.api.query
import dev.kord.core.behavior.MemberBehavior
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class MemberFeature(
    override val entity: MemberBehavior,
    override val features: MutableMap<MemberFeatureType<out EntityFeatureData<MemberBehavior>>, Boolean>
) : EntityFeature<MemberBehavior, MemberFeatureType<out EntityFeatureData<MemberBehavior>>>(entity, features) {

    companion object :
        EntityFeatureCompanion<MemberBehavior, MemberFeatureType<out EntityFeatureData<MemberBehavior>>, MemberFeature> {

        override val allFeatures: List<MemberFeatureType<out EntityFeatureData<MemberBehavior>>>
            get() = MemberFeatureType.features

        val dataDescription = description(MemberFeature::entity)

        override suspend fun createFeature(
            member: MemberBehavior,
            features: List<MemberFeatureType<out EntityFeatureData<MemberBehavior>>>
        ) = newSuspendedTransaction {
            for (feature in features) {
                ChannelSettings.new {
                    this.channelId = member.long
                    this.guildId = member.guild.long
                    this.type = feature.id
                    this.activated = false
                }
            }
            return@newSuspendedTransaction MemberFeature(
                member,
                MemberFeatureType.features.associateWith { false }.toMutableMap()
            )
        }

        override suspend fun DataCache.queryFromCache(entity: MemberBehavior): Query<MemberFeature> =
            query { MemberFeature::entity eq entity }

        override suspend fun DataCache.cache(feature: MemberFeature) {
            put(feature)
        }

        override suspend fun query(entity: MemberBehavior): MemberFeature {
            val channelId = entity.long
            val guildId = entity.guild.long
            val features = mutableMapOf<MemberFeatureType<out EntityFeatureData<MemberBehavior>>, Boolean>()
            return newSuspendedTransaction {
                val results =
                    ChannelSettings.find { (ChannelSettingsTable.guildId eq guildId) and (ChannelSettingsTable.channelId eq channelId) }
                if (results.empty()) {
                    return@newSuspendedTransaction createFeature(entity)
                } else {
                    results.onEach {
                        features[MemberFeatureType.fromStringOrElseThrow(it.type)] = it.activated
                    }
                    return@newSuspendedTransaction MemberFeature(entity, features)
                }
            }
        }
    }

    override suspend fun update() = newSuspendedTransaction {
        for ((feature, active) in features) {
            MemberSettings.find {
                (MemberSettingsTable.guildId eq entity.guild.long) and
                        (MemberSettingsTable.userId eq entity.long) and
                        (MemberSettingsTable.type eq feature.name)
            }.onEach {
                it.active = active
            }
        }
    }
}
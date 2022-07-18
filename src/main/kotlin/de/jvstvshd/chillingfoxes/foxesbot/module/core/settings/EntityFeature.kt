/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.core.settings

import com.kotlindiscord.kord.extensions.utils.getKoin
import dev.kord.cache.api.DataCache
import dev.kord.cache.api.Query
import dev.kord.core.Kord
import dev.kord.core.entity.KordEntity

abstract class EntityFeature<out ENTITY : KordEntity, TYPE : EntityFeatureType<EntityFeatureData<out ENTITY>, out ENTITY>>(
    open val entity: ENTITY,
    open val features: MutableMap<TYPE, Boolean>
) {

    interface EntityFeatureCompanion<ENTITY : KordEntity, TYPE : EntityFeatureType<EntityFeatureData<out ENTITY>, out ENTITY>, FEATURE : EntityFeature<ENTITY, TYPE>> {

        val allFeatures: List<TYPE>

        suspend fun feature(entity: ENTITY): FEATURE {
            val kord: Kord by getKoin().inject()
            val queryItem = kord.cache.queryFromCache(entity).singleOrNull()
            if (queryItem != null) {
                return completed(queryItem, kord.cache)
            }
            return completed(query(entity), kord.cache, true)
        }

        private suspend fun completed(
            feature: FEATURE,
            cache: DataCache,
            cacheDefault: Boolean = false
        ): FEATURE {
            val missingFeatureTypes = allFeatures - feature.features.keys
            return if (missingFeatureTypes.isEmpty()) {
                if (cacheDefault) {
                    cache.cache(feature)
                }
                feature
            } else {
                val completedFeature = createFeature(feature.entity, missingFeatureTypes)
                cache.cache(completedFeature)
                completedFeature
            }
        }

        suspend fun createFeature(
            member: ENTITY,
            features: List<TYPE> = allFeatures
        ): FEATURE

        suspend fun DataCache.queryFromCache(entity: ENTITY): Query<FEATURE>

        suspend fun DataCache.cache(feature: FEATURE)

        suspend fun query(entity: ENTITY): FEATURE
    }

    abstract suspend fun update()

    open fun isFeatureEnabled(type: TYPE) = features[type] == true
}
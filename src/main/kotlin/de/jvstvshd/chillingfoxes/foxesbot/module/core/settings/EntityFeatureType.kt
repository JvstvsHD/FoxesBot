/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.core.settings

import dev.kord.core.entity.KordEntity

interface EntityFeatureType<out T : EntityFeatureData<ENTITY>, ENTITY : KordEntity> {

    val id: String
    val name: String
    val description: String

}

interface EntityFeatureData<ENTITY : KordEntity> {
    val entity: ENTITY
}
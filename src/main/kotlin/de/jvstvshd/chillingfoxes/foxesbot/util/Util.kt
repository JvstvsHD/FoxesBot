/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.util

import dev.kord.common.entity.ChannelType
import kotlinx.datetime.Clock
import kotlin.reflect.KProperty1

@Suppress("UNCHECKED_CAST")
fun <R> Any.instanceOf(propertyName: String): R? {
    val property = this::class.members.firstOrNull { it.name == propertyName } as KProperty1<Any, *>?
    return property?.get(this) as R?
}

val guildChannelTypes = setOf<ChannelType>(
    ChannelType.GuildCategory,
    ChannelType.GuildDirectory,
    ChannelType.GuildNews,
    ChannelType.GuildStageVoice,
    ChannelType.GuildText,
    ChannelType.GuildVoice
)

val threadChannelTypes = setOf(ChannelType.PrivateThread, ChannelType.PublicGuildThread, ChannelType.PublicNewsThread)

fun now() = Clock.System.now()

const val FOXES_BOT_TIME_ZONE_ID = "Europe/Berlin"
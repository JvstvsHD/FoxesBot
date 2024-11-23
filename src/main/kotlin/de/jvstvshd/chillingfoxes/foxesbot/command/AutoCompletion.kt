/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.command

import de.jvstvshd.chillingfoxes.foxesbot.io.Event
import de.jvstvshd.chillingfoxes.foxesbot.io.EventType
import dev.kord.core.entity.interaction.AutoCompleteInteraction
import dev.kordex.core.utils.FilterStrategy
import dev.kordex.core.utils.suggestStringMap
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun AutoCompleteInteraction.suggestEvents() = newSuspendedTransaction {
    EventType.all().map { it.name }
    val result = Event.all().associate { event ->
        event.string() to event.codeName
    }
    suggestStringMap(result, FilterStrategy.Contains)
}
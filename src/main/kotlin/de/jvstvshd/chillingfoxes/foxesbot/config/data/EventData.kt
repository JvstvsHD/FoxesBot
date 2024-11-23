/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.config.data

import de.jvstvshd.chillingfoxes.foxesbot.module.event.countdown.CountdownResetState

@kotlinx.serialization.Serializable
data class EventData(
    var events: List<String> = listOf(),
    var countdownResetState: CountdownResetState = CountdownResetState.HUNDREDS_RESET_STATE
)

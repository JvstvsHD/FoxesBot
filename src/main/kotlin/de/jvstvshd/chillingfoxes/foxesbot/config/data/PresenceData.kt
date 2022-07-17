/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.config.data

import dev.kord.common.entity.ActivityType
import dev.kord.common.entity.PresenceStatus

@kotlinx.serialization.Serializable
data class PresenceData(
    var status: PresenceStatus = PresenceStatus.Online,
    var activityType: ActivityType = ActivityType.Custom,
    var name: String = "",
    var url: String? = null
)

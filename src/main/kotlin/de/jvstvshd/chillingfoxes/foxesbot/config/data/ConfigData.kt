/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.config.data

@kotlinx.serialization.Serializable
data class ConfigData(
    val dataBaseData: DataBaseData = DataBaseData(),
    val baseData: BaseData = BaseData(),
    val offlineCheckerData: OfflineCheckerData = OfflineCheckerData(),
    val eventData: EventData = EventData(),
    val moderationData: ModerationData = ModerationData(),
    var configVersion: Int = -1,
    val presenceData: PresenceData = PresenceData()
)
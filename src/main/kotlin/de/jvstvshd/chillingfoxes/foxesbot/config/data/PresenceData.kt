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

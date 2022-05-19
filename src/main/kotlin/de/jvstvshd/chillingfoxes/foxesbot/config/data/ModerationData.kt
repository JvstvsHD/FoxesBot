package de.jvstvshd.chillingfoxes.foxesbot.config.data

@kotlinx.serialization.Serializable
data class ModerationData(var criminalRecordChannel: Long = -1, var criminalRecordGuild: Long = -1)
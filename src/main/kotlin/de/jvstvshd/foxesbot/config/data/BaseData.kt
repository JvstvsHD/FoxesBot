package de.jvstvshd.foxesbot.config.data

data class BaseData(
    val token: String = "",
    val testGuildId: Long? = null,
    val gitHubToken: String = "",
    val prefix: String = ""
)

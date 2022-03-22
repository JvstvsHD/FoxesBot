package de.jvstvshd.chillingfoxes.foxesbot.config.data

@kotlinx.serialization.Serializable
data class BaseData(
    val token: String = "",
    val testGuildId: Long? = null,
    val gitHubToken: String = "",
    val prefix: String = "",
    var loggingWebhook: String? = null
)

package de.jvstvshd.chillingfoxes.foxesbot.config.data

@kotlinx.serialization.Serializable
data class DataBaseData(
    val host: String = "127.0.0.1",
    val password: String = "<password>",
    val username: String = "root",
    val database: String = "foxes_bot",
    val port: String = "3306",
    val maxPoolSize: Int = 10,
    val minimumIdle: Int = 2
)
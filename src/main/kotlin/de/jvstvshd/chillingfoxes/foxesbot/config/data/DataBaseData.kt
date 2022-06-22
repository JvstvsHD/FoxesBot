/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

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
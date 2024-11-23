/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.io

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import de.jvstvshd.chillingfoxes.foxesbot.config.data.DataBaseData
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun setupDatabase(dataBaseData: DataBaseData) {
    Database.connect(createDataSource(dataBaseData))
    initDatabase()
}

suspend fun initDatabase() = newSuspendedTransaction {
    SchemaUtils.createMissingTablesAndColumns(
        StatusAliasesTable,
        ChannelBarriersTable,
        MusicTable,
        EventTable,
        ChannelSettingsTable,
        MemberSettingsTable,
        EventParticipantTable
    )
}

private fun createDataSource(dataBaseData: DataBaseData) = with(dataBaseData) {
    val jdbcUrl = "jdbc:mariadb://$host:$port/$database?" + dataBaseData.fullProperties().entries.joinToString("&") {
        "${it.key}=${it.value}"
    }
    val config = HikariConfig()
    config.jdbcUrl = jdbcUrl
    return@with HikariDataSource(config)
}
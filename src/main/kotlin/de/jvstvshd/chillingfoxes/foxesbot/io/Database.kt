/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.io

import de.chojo.sqlutil.databases.SqlType
import de.chojo.sqlutil.datasource.DataSourceCreator
import de.jvstvshd.chillingfoxes.foxesbot.config.data.DataBaseData
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun setupDatabase(dataBaseData: DataBaseData) {
    Database.connect(createDataSource(dataBaseData))
    initDatabase()
}

suspend fun initDatabase() = newSuspendedTransaction {
    SchemaUtils.create(
        StatusAliasesTable,
        ChannelBarriersTable,
        MusicTable,
        EventDataTable,
        ChannelSettingsTable,
        MemberSettingsTable
    )
}

private fun createDataSource(dataBaseData: DataBaseData) = with(dataBaseData) {
    DataSourceCreator.create(SqlType.MARIADB).configure {
        it.host(host).port(port).database(database).user(username).password(password)
    }.create().withMaximumPoolSize(maxPoolSize).withMinimumIdle(minimumIdle).build()
}
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
    SchemaUtils.create(StatusAliasesTable, PresenceStatusTable, ChannelBarriersTable, MusicTable, EventDataTable)
}

private fun createDataSource(dataBaseData: DataBaseData) = with(dataBaseData) {
    DataSourceCreator.create(SqlType.MARIADB).configure {
        it.host(host).port(port).database(database).user(username).password(password)
    }.create().withMaximumPoolSize(maxPoolSize).withMinimumIdle(minimumIdle).build()
}
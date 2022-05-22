package de.jvstvshd.chillingfoxes.foxesbot.io

import com.zaxxer.hikari.HikariDataSource
import de.chojo.sqlutil.databases.SqlType
import de.chojo.sqlutil.datasource.DataSourceCreator
import de.jvstvshd.chillingfoxes.foxesbot.config.data.DataBaseData
import dev.kord.core.kordLogger
import org.apache.ibatis.jdbc.ScriptRunner
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.io.InputStreamReader

@Deprecated(message = "Consider migration to Exposed. This variable does only exist to create backwards compatibility. It is only used by the offline checker module which needs a rewrite before.")
lateinit var _dataSource: HikariDataSource
    private set

suspend fun setupDatabase(dataBaseData: DataBaseData) {
    org.jetbrains.exposed.sql.Database.connect(createDataSource(dataBaseData).also { _dataSource = it })
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

@Deprecated(
    message = "Replacement with above methods since an instance of a DataSource is no longer needed due to migration to JetBrains Composed",
    replaceWith = ReplaceWith("setupDatabase")
)
class Database(private val dataBaseData: DataBaseData) {

    @Deprecated(message = "Replacement with JetBrains Exposed")
    val dataSource = createDataSource()

    init {
        kordLogger.error("Consider migration to Exposed.")
        init()
    }

    @Deprecated("Consider migration to Exposed.")
    private fun createDataSource(): HikariDataSource {
        /*val properties = Properties()
        properties.setProperty("dataSource.databaseName", dataBaseData.database)
        properties.setProperty("dataSource.serverName", dataBaseData.host)
        properties.setProperty("dataSource.portNumber", dataBaseData.port)
        properties.setProperty("dataSourceClassName", MariaDbDataSource::class.java.name)
        properties.setProperty("dataSource.user", dataBaseData.username)
        properties.setProperty("dataSource.password", dataBaseData.password)
        val config = HikariConfig(properties)
        config.poolName = "foxesbot-hikari"
        *//*val ds = HikariDataSource()
        with(dataBaseData) {
            //config.ur = "jdbc:mysql://$host:$port/$database"
            ds.addDataSourceProperty("url", "jdbc:mysql://$host:$port/$database")
            ds.dataSourceClassName = MariaDbDataSource::class.java.name
            ds.username = username
            ds.password = password
        }*//*
        return HikariDataSource(config)*/
        return _dataSource
    }

    @Deprecated(message = "Replacement with JetBrains Exposed")
    private fun init() {
        dataSource.connection.use {
            ScriptRunner(it).runScript(
                InputStreamReader(
                    de.jvstvshd.chillingfoxes.foxesbot.FoxesBot::class.java.getResourceAsStream(
                        "/init.sql"
                    )!!
                )
            )
        }
    }
}
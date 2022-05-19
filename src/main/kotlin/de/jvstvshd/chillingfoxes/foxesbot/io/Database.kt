package de.jvstvshd.chillingfoxes.foxesbot.io

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import de.jvstvshd.chillingfoxes.foxesbot.config.data.DataBaseData
import org.apache.ibatis.jdbc.ScriptRunner
import org.mariadb.jdbc.MariaDbDataSource
import java.io.InputStreamReader
import java.util.*

class Database(private val dataBaseData: DataBaseData) {

    @Deprecated(message = "Replacement with JetBrains Exposed", level = DeprecationLevel.ERROR)
    val dataSource = createDataSource()

    init {
        init()
    }

    @Deprecated(message = "Replacement with JetBrains Exposed", level = DeprecationLevel.ERROR)
    private fun createDataSource(): HikariDataSource {
        val properties = Properties()
        properties.setProperty("dataSource.databaseName", dataBaseData.database)
        properties.setProperty("dataSource.serverName", dataBaseData.host)
        properties.setProperty("dataSource.portNumber", dataBaseData.port)
        properties.setProperty("dataSourceClassName", MariaDbDataSource::class.java.name)
        properties.setProperty("dataSource.user", dataBaseData.username)
        properties.setProperty("dataSource.password", dataBaseData.password)
        val config = HikariConfig(properties)
        config.poolName = "foxesbot-hikari"
        return HikariDataSource(config)
    }

    @Deprecated(message = "Replacement with JetBrains Exposed", level = DeprecationLevel.ERROR)
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
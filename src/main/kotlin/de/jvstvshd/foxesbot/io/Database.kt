package de.jvstvshd.foxesbot.io

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import de.jvstvshd.foxesbot.config.data.DataBaseData
import org.mariadb.jdbc.MariaDbDataSource
import java.util.*

class Database(private val dataBaseData: DataBaseData) {

    val dataSource = createDataSource()

    init {
        init()
    }

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

    private fun init() {
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS updates " +
                        "(" +
                        "url VARCHAR (1024) NOT NULL," +
                        "type VARCHAR (100) NOT NULL," +
                        "primary key(type)" +
                        ");"
            ).use { preparedStatement ->
                preparedStatement.executeUpdate()
            }
        }
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS update_tracker_subscriptions" +
                        "(" +
                        "id BIGINT NOT NULL," +
                        "type CHAR (255) NOT NULL," +
                        "CONSTRAINT update_tracker_subscriptions_constraint " +
                        "UNIQUE (type, id)" +
                        ");"
            ).use { preparedStatement -> preparedStatement.executeUpdate() }
        }
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS status_aliases" +
                        "(" +
                        "name CHAR (255) NOT NULL," +
                        "url CHAR (255) NOT NULL," +
                        "type CHAR (255) NOT NULL," +
                        "primary key (name)" +
                        ");"
            ).use { preparedStatement -> preparedStatement.executeUpdate() }
        }
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS presence_status" +
                        "(" +
                        "id BIGINT NOT NULL," +
                        "status CHAR (255) NOT NULL," +
                        "primary key (id)" +
                        ");"
            ).use {
                it.executeUpdate()
            }
        }
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS offline_checker" +
                        "(" +
                        "id BIGINT NOT NULL," +
                        "suppressed BOOLEAN NOT NULL," +
                        "banned BOOLEAN NOT NULL," +
                        "type CHAR (64)," +
                        "primary key (id)" +
                        ");"
            ).use {
                it.executeUpdate()
            }
        }
    }
}
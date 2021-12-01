package de.jvstvshd.foxesbot.module.christmas

import com.zaxxer.hikari.HikariDataSource
import dev.kord.common.entity.Snowflake

class StatisticService(private val dataSource: HikariDataSource) {

    fun log(type: StatisticType, id: Snowflake, count: Int) =
        dataSource.connection.use { connection ->
            connection.prepareStatement("INSERT INTO christmas_stats (type, id, count) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE count = count + ?;")
                .use {
                    it.setString(1, type.name)
                    it.setLong(2, id.value.toLong())
                    it.setInt(3, count)
                    it.setInt(4, count)
                    /*it.setString(5, type.name)
                    it.setLong(6, id.value.toLong())*/
                    it.executeUpdate()
                }
        }

}
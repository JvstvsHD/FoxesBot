package de.jvstvshd.foxesbot.module.christmas.statistic

import com.zaxxer.hikari.HikariDataSource
import dev.kord.common.entity.Snowflake

class StatisticService(private val dataSource: HikariDataSource) {

    fun log(type: StatisticType, id: Snowflake, count: Int) =
        dataSource.connection.use { connection ->
            connection.prepareStatement("INSERT INTO christmas_stats (type, id, count) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE count = count + ?;")
                .use {
                    val amount =
                        if (type is ThrownSnowballCount) {
                            it.setString(1, type.name + "_" + count)
                            1
                        } else {
                            it.setString(1, type.name)
                            count
                        }
                    it.setLong(2, id.value.toLong())
                    it.setInt(3, amount)
                    it.setInt(4, amount)
                    it.executeUpdate()
                }
        }

}
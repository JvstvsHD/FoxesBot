package de.jvstvshd.foxesbot.module.core.music

import com.kotlindiscord.kord.extensions.utils.runSuspended
import com.zaxxer.hikari.HikariDataSource

class MusicService(private val dataSource: HikariDataSource) {

    suspend fun getUrls(topic: String?) = getUrls(topic, MusicState.ACTIVATED)

    suspend fun getUrls(topic: String?, state: MusicState) =
        runSuspended {
            dataSource.connection.use { connection ->
                val query =
                    if (topic != null) "SELECT url FROM music WHERE topic = ? AND state = ?" else "SELECT url FROM music WHERE state = ?;"
                connection.prepareStatement(query).use {
                    if (topic == null) {
                        it.setString(1, state.name)
                    } else {
                        it.setString(1, topic)
                        it.setString(2, state.name)
                    }
                    val rs = it.executeQuery()
                    val list = mutableListOf<String>()
                    while (rs.next()) {
                        list.add(rs.getString(1))
                    }
                    return@runSuspended list
                }
            }
        }

    suspend fun getNames(topic: String?, state: MusicState) =
        runSuspended {
            dataSource.connection.use { connection ->
                val query =
                    if (topic != null) "SELECT name FROM music WHERE topic = ? AND state = ?" else "SELECT name FROM music WHERE state = ?;"
                connection.prepareStatement(query).use {
                    if (topic == null) {
                        it.setString(1, state.name)
                    } else {
                        it.setString(1, topic)
                        it.setString(2, state.name)
                    }
                    val rs = it.executeQuery()
                    val list = mutableListOf<String>()
                    while (rs.next()) {
                        list.add(rs.getString(1))
                    }
                    return@runSuspended list
                }
            }
        }

    suspend fun getNames(topic: String?) = getNames(topic, MusicState.ACTIVATED)

    suspend fun reactivateAll() = runSuspended {
        dataSource.connection.use { connection ->
            connection.prepareStatement("UPDATE music SET state = ?").use {
                it.setString(1, MusicState.ACTIVATED.name)
                it.executeUpdate()
            }
        }
    }

}
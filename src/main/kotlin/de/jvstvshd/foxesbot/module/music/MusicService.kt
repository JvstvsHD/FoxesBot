package de.jvstvshd.foxesbot.module.music

import com.kotlindiscord.kord.extensions.utils.runSuspended
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.runBlocking

class MusicService(private val dataSource: HikariDataSource) {

    suspend fun retrieveMusicTracks(topic: String? = null, state: MusicState? = null): List<MusicTrackInfo> = runBlocking {
        val query = "SELECT * FROM music" + if (topic == null) ";" else " WHERE topic = ?;"
        val list = mutableListOf<MusicTrackInfo>()
        dataSource.connection.use { connection ->
            connection.prepareStatement(query).use {
                topic?.let { topic ->
                    it.setString(1, topic)
                }
                val rs = it.executeQuery()
                while (rs.next()) {

                }
            }
        }
        return@runBlocking list
    }

    suspend fun getUrls(topic: String?) = getUrls(topic, MusicState.ACTIVATED)

    private suspend fun getUrls(topic: String?, state: MusicState) =
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

    suspend fun changeState(column: String, columnValue: String, state: MusicState) = runSuspended {
        dataSource.connection.use { connection ->
            connection.prepareStatement("UPDATE music SET state = ? WHERE $column = ?").use {
                it.setString(1, state.name)
                it.setString(2, columnValue)
                it.executeUpdate()
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

    suspend fun deleteByTopic(topic: String) = delete(createStatement("topic"), topic)

    private fun createStatement(column: String) = "DELETE FROM music WHERE $column = ?"

    suspend fun delete(query: String, value: String) = runSuspended {
        dataSource.connection.use { connection ->
            connection.prepareStatement(query).use {
                it.setString(1, value)
                return@runSuspended it.executeUpdate()
            }
        }
    }

    suspend fun deleteByName(name: String) = delete(createStatement("name"), name)

    suspend fun deleteByUrl(url: String) = delete(createStatement("url"), url)

}
package de.jvstvshd.chillingfoxes.foxesbot.module.core.music

import de.jvstvshd.chillingfoxes.foxesbot.io.Music
import de.jvstvshd.chillingfoxes.foxesbot.io.MusicTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction


class MusicService {

    suspend fun getMusicEntities(
        sqlExpressionBuilder: (SqlExpressionBuilder.() -> Op<Boolean>)? = null,
        builder: SizedIterable<Music>.() -> List<Music> = { toList() }
    ): List<Music> = newSuspendedTransaction {
        if (sqlExpressionBuilder == null) {
            return@newSuspendedTransaction builder(Music.all())
        }
        builder(Music.find(sqlExpressionBuilder))
    }

    @Deprecated(message = "replacement with Exposed functions", level = DeprecationLevel.ERROR)
    private suspend fun getUrls(topic: String?, state: MusicState) = newSuspendedTransaction {
        if (topic != null) {
            return@newSuspendedTransaction Music.find { (MusicTable.topic eq topic) and (MusicTable.state eq state.name) }
                .map { music -> music.url }
        } else {
            return@newSuspendedTransaction Music.find { MusicTable.state eq state.name }
                .map { music: Music -> music.url }
        }
    }

    @Deprecated(message = "replacement with Exposed functions", level = DeprecationLevel.ERROR)
    private suspend fun getStringColumn(column: String, topic: String?, state: MusicState) = newSuspendedTransaction {
        //Music.find
    }
    /* runSuspended {
        dataSource.connection.use { connection ->
            val query = buildQuery(column, topic)
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
    }*/


    @Suppress("UNCHECKED_CAST")
    suspend fun changeState(state: MusicState, sqlExpressionBuilder: (SqlExpressionBuilder.() -> Op<Boolean>)? = null) =
        newSuspendedTransaction {
            getMusicEntities(sqlExpressionBuilder).forEach { music ->
                music.state = state.name.lowercase()
            }
        }

    /*suspend fun reactivateAll() = runSuspended {

    }*/

    suspend fun deleteByTopic(topic: String) = delete { MusicTable.topic eq topic }

    private suspend fun delete(sqlExpressionBuilder: SqlExpressionBuilder.() -> Op<Boolean>) =
        getMusicEntities(sqlExpressionBuilder).forEach { music: Music -> music.delete() }


    suspend fun deleteByName(name: String) = delete { MusicTable.name eq name }


    suspend fun deleteByUrl(url: String) = delete { MusicTable.url eq url }

}
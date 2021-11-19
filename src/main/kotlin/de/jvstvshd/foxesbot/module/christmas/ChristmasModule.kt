package de.jvstvshd.foxesbot.module.christmas

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.utils.runSuspended
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import com.zaxxer.hikari.HikariDataSource
import de.jvstvshd.foxesbot.utils.KordUtil.toLong
import de.jvstvshd.foxesbot.utils.KordUtil.toSnowflake
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.channel.TextChannel
import kotlinx.coroutines.flow.collect
import java.time.LocalTime
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class ChristmasModule(private val executor: ScheduledExecutorService, val dataSource: HikariDataSource) :
    Extension() {

    override val name = "christmas"
    private val scheduler = Scheduler()
    private val lock = ReentrantReadWriteLock()

    @OptIn(ExperimentalTime::class)
    override suspend fun setup() {
        startTimer()
        throwCommand()
        throwChatCommand()
    }

    @OptIn(ExperimentalTime::class)
    private fun startTimer() {
        var firstStarted = true
        scheduler.schedule(Duration.minutes(60)) {
            if (firstStarted) {
                firstStarted = false
                kotlinx.coroutines.delay(Duration.minutes(60 - LocalTime.now().minute))
            }
            if (LocalTime.now().hour == 6) {
                kord.guilds.collect { guild ->
                    guild.members.collect { member ->
                        dataSource.connection.use { connection ->
                            connection.prepareStatement("INSERT INTO snowballs (user_id, snowballs) VALUES (?, ?) ON DUPLICATE KEY UPDATE snowballs = ?;")
                                .use {
                                    it.setLong(1, member.toLong())
                                    it.setLong(2, 6)
                                    it.setLong(3, 6)
                                    it.executeUpdate()
                                    getChannel()?.createMessage {
                                        content = "Es hat geschneit :snowflake:"
                                    }
                                }
                        }
                    }
                }
            }
        }
    }

    suspend fun checkChannel(channel: Channel, name: String): Boolean =
        runSuspended {
            dataSource.connection.use { connection ->
                connection.prepareStatement("SELECT FROM channel_barriers WHERE name = ? AND channel_id = ?;").use {
                    it.setString(1, name)
                    it.setLong(2, channel.toLong())
                    return@runSuspended it.executeQuery().next()
                }

            }
        }

    suspend fun getChannel() = runSuspended {
        dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT channel_id FROM channel_barriers WHERE name = ?;").use {
                it.setString(1, "christmas")
                val rs = it.executeQuery()
                return@runSuspended if (!rs.next()) null else kord.getChannel(
                    rs.getLong(1).toSnowflake()
                ) as TextChannel
            }
        }
    }

    suspend fun changeSnowMonster(guildId: Long, subtrahend: Int): Int = runSuspended {
        lock.writeLock().lock()
        dataSource.connection.use { connection ->
            connection.prepareStatement("INSERT INTO snow_monster (guild_id, hp) VALUES (?, ?) ON DUPLICATE KEY UPDATE hp = hp - ?;")
                .use {
                    it.setLong(1, guildId)
                    it.setInt(2, 1000 - subtrahend)
                    it.setInt(3, subtrahend)
                    it.executeUpdate()
                    lock.writeLock().unlock()
                }
            connection.prepareStatement("SELECT hp FROM snow_monster WHERE guild_id = ?;").use {
                it.setLong(1, guildId)
                val rs = it.executeQuery()
                if (rs.next())
                    return@runSuspended rs.getInt(1)
                return@runSuspended -1
            }
        }
    }
}
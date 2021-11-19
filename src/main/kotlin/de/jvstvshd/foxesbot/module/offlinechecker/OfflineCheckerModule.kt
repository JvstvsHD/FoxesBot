package de.jvstvshd.foxesbot.module.offlinechecker

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import com.zaxxer.hikari.HikariDataSource
import de.jvstvshd.foxesbot.utils.KordUtil.toLong
import de.jvstvshd.foxesbot.utils.KordUtil.toPresenceStatus
import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.user.PresenceUpdateEvent
import dev.kord.core.event.user.VoiceStateUpdateEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import java.util.concurrent.Executor
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class OfflineCheckerModule(private val dataSource: HikariDataSource, private val executor: Executor) : Extension() {

    override val name = "offline_checker"
    override val bundle = "offline_checker"
    private val offlineCheckers = mutableMapOf<Snowflake, OfflineChecker>()
    private val scheduler: Scheduler = Scheduler()

    @OptIn(ExperimentalTime::class, kotlinx.coroutines.DelicateCoroutinesApi::class)
    override suspend fun setup() {
        event<VoiceStateUpdateEvent> {
            action {
                println(event.state.data.channelId)
            }
        }
        event<ReadyEvent> {
            action {
                GlobalScope.launch {
                    async {
                        delay(Duration.Companion.seconds(5))
                        check(kord)
                    }
                }
            }
        }
        event<PresenceUpdateEvent> {
            action {
                GlobalScope.async {
                    dataSource.connection.use { connection ->
                        try {
                            connection.prepareStatement("INSERT INTO presence_status (id, status) VALUES (?, ?) ON DUPLICATE KEY UPDATE status = ?;")
                                .use {
                                    it.setLong(1, event.getUser().toLong())
                                    it.setString(2, event.presence.status.value)
                                    it.setString(3, event.presence.status.value)
                                    it.executeUpdate()
                                }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                if (event.presence.status == PresenceStatus.Offline && event.member.getVoiceStateOrNull()?.channelId != null) {
                    getOrCreateOfflineChecker(event.getMember()).start()
                }
            }
        }
    }

    private suspend fun check(kord: Kord) {
        kord.guilds.collect { guild ->
            checkGuild(guild)
        }
    }

    private suspend fun checkGuild(guild: Guild) {
        guild.members.filter { member -> member.getVoiceStateOrNull() != null }.collect { member ->
            checkMember(member)
        }
    }

    private suspend fun checkMember0(member: Member, status: PresenceStatus) {
        println(status)
        if (status == PresenceStatus.Offline) {
            println("offline: " + member.username)
            getOrCreateOfflineChecker(member).start()
        }
    }

    private fun getOrCreateOfflineChecker(member: Member) =
        offlineCheckers[member.id] ?: OfflineChecker(member, scheduler, this).also {
            offlineCheckers[member.id] = it
        }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun checkMember(member: Member) {
        val presence: PresenceStatus
        println(member)
        if (member.getPresenceOrNull() == null) {
            GlobalScope.async {
                dataSource.connection.use { connection ->
                    try {
                        connection.prepareStatement("SELECT status FROM presence_status WHERE id = ?;").use {
                            it.setLong(1, member.toLong())
                            val resultSet = it.executeQuery()
                            val value: String
                            if (resultSet.next()) {
                                value = resultSet.getString(1)
                            } else {
                                value = "offline"
                                connection.prepareStatement("INSERT INTO presence_status (id, status) VALUES (?, ?);")
                                    .use { ps ->
                                        ps.setLong(1, member.toLong())
                                        ps.setString(2, "offline")
                                        ps.executeUpdate()
                                    }
                            }

                            println("value = $value")
                            checkMember0(
                                member,
                                value.toPresenceStatus()
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } else {
            checkMember0(member, member.getPresence().status)
        }
    }

    fun isMemberBanned(member: Member): Boolean {
        dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT banned FROM offline_checker WHERE id = ?").use { statement ->
                statement.setLong(1, member.toLong())
                val resultSet = statement.executeQuery()
                return resultSet.next()
            }
        }
    }

    fun voiceBan(member: Member) {
        executor.execute {
            dataSource.connection.use { connection ->
                connection.prepareStatement("INSERT INTO offline_checker (id, suppressed, banned, type) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE banned = ?;")
                    .use {
                        it.setLong(1, member.toLong())
                        it.setBoolean(2, false)
                        it.setBoolean(3, true)
                        it.setString(4, "member")
                        it.setBoolean(5, true)
                    }
            }
        }
    }
}
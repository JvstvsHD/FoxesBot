package de.jvstvshd.foxesbot.module.christmas

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.runSuspended
import com.zaxxer.hikari.HikariDataSource
import de.jvstvshd.foxesbot.config.Config
import de.jvstvshd.foxesbot.module.christmas.commands.*
import de.jvstvshd.foxesbot.module.core.music.MusicPlayer
import de.jvstvshd.foxesbot.module.core.music.MusicService
import de.jvstvshd.foxesbot.util.KordUtil.toLong
import de.jvstvshd.foxesbot.util.KordUtil.toSnowflake
import de.jvstvshd.foxesbot.util.limit.Limitation
import de.jvstvshd.foxesbot.util.limit.LocalTimeBasedLimitation
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.optional
import dev.kord.core.behavior.channel.BaseVoiceChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.StageChannel
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.rest.json.request.CurrentVoiceStateModifyRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

class ChristmasModule(
    val dataSource: HikariDataSource,
    val config: Config
) :
    Extension() {

    override val name = "christmas"
    private val lock = ReentrantReadWriteLock()
    val christmasTimes = mutableMapOf<Snowflake, MusicPlayer>()
    private val musicService = MusicService(dataSource)

    @OptIn(ExperimentalTime::class)
    override suspend fun setup() {
        event<VoiceStateUpdateEvent> {
            action {
                if (event.state.userId == kord.selfId) {
                    if (event.state.channelId == null) {
                        christmasTimes[event.state.guildId]?.exit()
                    }
                }
            }
        }
        Runtime.getRuntime().addShutdownHook(Thread() {
            for (christmasTime in christmasTimes) {
                val channel = christmasTime.value.channel
                runBlocking {
                    if (channel is StageChannel) {
                        channel.getStageInstanceOrNull()?.delete("Delete on exit")
                    }
                }
            }
        })
        startTimer()
        throwCommand()
        throwChatCommand()
        administrationCommands()
        christmasMusicCommands()
        christmasTimeCommand()
        refillCommand()
    }

    suspend fun createMusicPlayer(channel: BaseVoiceChannelBehavior, limitation: Limitation): MusicPlayer {
        val player = christmasTimes[channel.guildId] ?: ChristmasMusicPlayer(
            channel,
            musicService,
            this,
            limitation
        ).also {
            christmasTimes[channel.guildId] = it
        }
        player.exit()
        return player
    }

    @OptIn(ExperimentalTime::class, DelicateCoroutinesApi::class)
    private fun startTimer() {
        GlobalScope.launch {
            if (LocalTime.now().hour in 18..20) {
                startChristmasTime()
            }
            val delay = (60 - LocalTime.now().hour)
            println("delay = $delay")
            println("hour: " + LocalTime.now().hour)
            delay(delay.minutes)
            while (true) {
                val hour = LocalTime.now().hour
                println("hour = $hour")
                if (hour == 6) {
                    refill()
                } else if (hour in 18..20) {
                    println("Starting Christmas time....")
                    startChristmasTime()
                }
                delay(60.minutes)
            }
        }
    }

    private suspend fun startChristmasTime() {
        kord.guilds.collect { guild ->
            guild.channels.filter { it is StageChannel }.firstOrNull()?.let {
                christmasTime(it as StageChannel)
            }
        }
    }

    @OptIn(KordVoice::class)
    suspend fun christmasTime(channel: StageChannel) {
        val player = createMusicPlayer(channel, LocalTimeBasedLimitation(LocalTime.of(20, 0)))
        player.exit()
        player.playRandom("christmas")
        if (channel.getStageInstanceOrNull() == null) {
            channel.createStageInstance("Weihnachtsmusik")
        }
        channel.kord.rest.guild.modifyCurrentVoiceState(
            channel.guildId,
            CurrentVoiceStateModifyRequest(channel.id, suppress = false.optional())
        )
    }

    suspend fun refill() {
        kord.guilds.collect { guild ->
            guild.members.collect { member ->
                dataSource.connection.use { connection ->
                    connection.prepareStatement("INSERT INTO snowballs (id, snowballs) VALUES (?, ?) ON DUPLICATE KEY UPDATE snowballs = ?;")
                        .use {
                            try {
                                it.setLong(1, member.toLong())
                                it.setLong(2, config.configData.eventData.snowballLimit.toLong())
                                it.setLong(3, config.configData.eventData.snowballLimit.toLong())
                                it.executeUpdate()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                }
            }
            document(guild.toLong(), "refill")
            getChannel(guild.toLong())?.createMessage {
                content = "Es hat geschneit :snowflake:"
            }
        }
    }

    suspend fun document(id: Long, type: String) = runSuspended {
        dataSource.connection.use { connection ->
            connection.prepareStatement("INSERT INTO snowball_action (id, lastDate, type) VALUES (?, ?, ?);")
                .use {
                    try {
                        it.setLong(1, id)
                        it.setTimestamp(2, Timestamp.from(Instant.now()))
                        it.setString(3, type)
                        it.executeUpdate()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        }
    }

    suspend fun lastTime(memberId: Long, type: String): LocalDateTime? = runSuspended {
        dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT lastDate FROM snowball_action WHERE id = ? AND type = ? ORDER BY lastDate DESC;")
                .use {
                    it.setLong(1, memberId)
                    it.setString(2, type)
                    val rs = it.executeQuery()
                    if (rs.next()) {
                        return@runSuspended rs.getTimestamp(1).toLocalDateTime()
                    }
                    return@runSuspended null
                }
        }
    }

    suspend fun getChannel(guildId: Long) = runSuspended {
        dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT channel_id FROM channel_barriers WHERE name = ? AND guild_id = ?;")
                .use {
                    it.setString(1, "christmas")
                    it.setLong(2, guildId)
                    val rs = it.executeQuery()
                    return@runSuspended if (!rs.next()) null else kord.getChannel(
                        rs.getLong(1).toSnowflake()
                    ) as TextChannel
                }
        }
    }

    @Suppress("GrazieInspection")
    suspend fun changeSnowMonster(guildId: Long, newHp: Int) = runSuspended {
        lock.writeLock().lock()
        dataSource.connection.use { connection ->
            connection.prepareStatement("INSERT INTO snow_monster (guild_id, hp) VALUES (?, ?) ON DUPLICATE KEY UPDATE hp = ?;")
                .use {
                    it.setLong(1, guildId)
                    it.setInt(2, newHp)
                    it.setInt(3, newHp)
                    it.executeUpdate()
                }

        }
        lock.writeLock().unlock()
    }

    suspend fun getSnowMonsterHp(guildId: Long): Int = runSuspended {
        dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT hp FROM snow_monster WHERE guild_id = ?;").use {
                it.setLong(1, guildId)
                val rs = it.executeQuery()
                return@runSuspended if (rs.next()) rs.getInt(1) else -1
            }
        }
    }
}
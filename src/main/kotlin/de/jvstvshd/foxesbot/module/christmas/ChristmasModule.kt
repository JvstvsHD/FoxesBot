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
import dev.kord.core.entity.Guild
import dev.kord.core.entity.channel.StageChannel
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.rest.json.request.CurrentVoiceStateModifyRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import org.apache.logging.log4j.LogManager
import java.sql.Timestamp
import java.time.*
import java.util.concurrent.locks.ReentrantReadWriteLock
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
    private val logger = LogManager.getLogger()
    val statisticService = StatisticService(dataSource)

    @OptIn(ExperimentalTime::class)
    override suspend fun setup() {
        event<VoiceStateUpdateEvent> {
            action {
                if (event.state.userId == kord.selfId) {
                    if (event.state.channelId == null) {
                        println("bot left channel")
                        christmasTimes[event.state.guildId]?.exit()
                    }
                }
            }
        }
        Runtime.getRuntime().addShutdownHook(Thread {
            for (christmasTime in christmasTimes) {
                val channel = christmasTime.value.channel
                runBlocking {
                    christmasTime.value.exit()
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

    @OptIn(DelicateCoroutinesApi::class)
    private fun startTimer() {
        runTimer(18, { it in 18..20 }, "Christmastime") {
            logger.debug("Starting Christmastime....")
            startChristmasTime()
        }
        runTimer(6, { it == 6 }, "refill", startTask = {
            refillSnowballs()
        }) {
            logger.debug("Refilling snowballs")
            refillSnowballs()
        }
    }

    private suspend fun refillSnowballs() {
        val results = refill()
        for (refillResult in results) {
            if (refillResult.exception != null) {
                val e = refillResult.exception
                logger.info("Refill skipped for guild ${e.guild.name} with id ${e.guild.toLong()}: ${e.message} (${e.userMessage})")
            } else {
                logger.info("Refilled for guild ${refillResult.guild.name} with id ${refillResult.guild.toLong()}")
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun runTimer(
        startHour: Int,
        predicate: suspend (Int) -> Boolean,
        name: String,
        startTask: (suspend () -> Unit)? = null,
        callback: suspend () -> Unit
    ) {
        GlobalScope.launch {
            val hour = LocalTime.now().hour
            val nextInvocation =
                if (predicate(hour)) {
                    try {
                        callback.invoke()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    LocalDateTime.now().plusDays(1).withHour(startHour).withMinute(0)
                } else {
                    val date = LocalDateTime.now().withHour(startHour).withMinute(0)
                    if (date.isBefore(LocalDateTime.now())) {
                        date.plusDays(1)
                    } else {
                        date
                    }
                }
            startTask?.invoke()

            val delay = Duration.between(LocalDateTime.now(), nextInvocation).toMillis()
            logger.debug("$name: $delay")
            if (delay < 0) {
                logger.debug("Cancelling $name task - delay < 0")
                cancel("delay may not be less than zero")
            }
            delay(delay)
            callback.invoke()
            while (true) {
                delay(24 * 60 * 60 * 1000)
                try {
                    callback.invoke()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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

    class RefillException(val userMessage: String, message: String?, val guild: Guild) : Exception(message)

    private suspend fun refill(): List<RefillResult> {
        val results = mutableListOf<RefillResult>()
        kord.guilds.collect { guild ->
            results.add(refill0(guild))
        }
        return results
    }

    private suspend fun checkGuildRefill(guild: Guild): Boolean {
        val lastTime = lastTime(guild.toLong(), "refill") ?: return true
        if (lastTime.toLocalDate().equals(LocalDate.now())) {
            throw RefillException("Cooldown - warte bis zum nächsten Tag", "on cooldown", guild)
        }
        if (LocalTime.now().hour < 6) {
            throw RefillException("Warte bis 6 Uhr", "wait to 6 AM", guild)
        }
        return true
    }

    class RefillResult(val guild: Guild, val exception: RefillException? = null)

    suspend fun refill0(guild: Guild): RefillResult {
        try {
            checkGuildRefill(guild)
        } catch (e: RefillException) {
            return RefillResult(guild, e)
        }
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
        try {
            document(guild.toLong(), "refill")
            getChannel(guild.toLong())?.createMessage {
                content = "Es hat geschneit :snowflake:"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return RefillResult(guild)
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
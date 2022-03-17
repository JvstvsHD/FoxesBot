package de.jvstvshd.chillingfoxes.foxesbot.module.event

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum
import com.kotlindiscord.kord.extensions.utils.dm
import com.kotlindiscord.kord.extensions.utils.respond
import com.notkamui.keval.KevalException
import com.notkamui.keval.keval
import com.zaxxer.hikari.HikariDataSource
import de.jvstvshd.chillingfoxes.foxesbot.config.data.ConfigData
import de.jvstvshd.chillingfoxes.foxesbot.util.JavaLocalDateTimeSerializer
import de.jvstvshd.chillingfoxes.foxesbot.util.KordUtil.toLong
import de.jvstvshd.chillingfoxes.foxesbot.util.TextChannelBehaviorSerializer
import de.jvstvshd.chillingfoxes.foxesbot.util.selfAuthor
import de.jvstvshd.chillingfoxes.foxesbot.util.standardDateTimeFormatter
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.TextChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.kordLogger
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import org.apache.commons.lang3.time.DurationFormatUtils
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import kotlin.math.roundToLong

val allowedDeletedMessages = mutableListOf<Snowflake>()

class CountdownEvent(
    val data: CountdownEventData,
    val configData: ConfigData,
    val dataSource: HikariDataSource,
    val kord: Kord
) {

    private var locked = true
    private var shouldBeSaved = true

    private fun serialize() = Json.encodeToString(data)

    suspend fun save() {
        kordLogger.info("attempting to save countdown event for channel ${data.channel.asChannel().name} in guild ${data.channel.guild.asGuild().name}")
        if (data.count == 0L || !shouldBeSaved) {
            kordLogger.info("skipping saving for ${data.channel.asChannel().name}. shouldBeSaved: $shouldBeSaved")
            return
        }
        try {
            dataSource.connection.use { connection ->
                connection.prepareStatement(
                    "INSERT INTO event_data (guild_id, channel_id, type, data) VALUES (?, ?, ?, ?) " +
                            "ON DUPLICATE KEY UPDATE data = ?"
                ).use { statement ->
                    statement.setLong(1, data.channel.guild.toLong())
                    statement.setLong(2, data.channel.toLong())
                    statement.setString(3, COUNTDOWN_EVENT_NAME)
                    val content = serialize()
                    statement.setString(4, content)
                    statement.setString(5, content)
                    kordLogger.info("saved for ${data.channel.asChannel().name}")
                    kordLogger.info("statement#executeUpdater(): ${statement.executeUpdate()}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        shouldBeSaved = false
    }

    suspend fun countdown(message: Message) {
        if (locked) {
            message.author?.dm("Bitte warte einen Moment. Sollte sich das Problem nicht in den nächsten Minuten beheben, melde dich bitte im Support!")
            return
        }
        if (message.author?.toLong() == data.lastUser) {
            message.allowAndDelete()
            message.author?.dm("Du musst warten, bis jemand anderes eine Nachricht schreibt.")
            return
        }
        message.author?.toLong()?.let {
            data.lastUser = it
        }
        val content = message.content
        val transmitted = checkForFail(content, message).let {
            if (it.type != null) {
                message.allowAndDelete()
                fail(
                    it.failMessage ?: "Äh... Das sollte nicht passieren (wahrscheinlicher Grund: ${it.type.name})",
                    null,
                    it.type,
                    message.author?.toLong() ?: -1
                )
                return
            }
            it.transmitted
        }
        synchronized(data.count) {
            setCount(transmitted)
            shouldBeSaved = true
        }
        if (data.count == 0L) {
            end()
        }
    }

    private suspend fun checkForFail(raw: String, message: Message): CheckResult {
        val transmitted = raw.toLongOrNull() ?: evaluate(raw, message)
        ?: return CheckResult(
            -1,
            "${message.author?.mention} hat leider keine Zahl abgesendet.",
            FailType.NotANumber
        )
        synchronized(data.count) {
            if (data.count - 1 != transmitted) {
                return CheckResult(
                    -1,
                    "${message.author?.mention} hat leider die falsche Zahl ($transmitted) abgesendet. Richtig: ${data.count - 1}",
                    FailType.WrongNumber
                )
            }
        }
        return CheckResult(transmitted, null)
    }

    private suspend fun evaluate(raw: String, message: Message): Long? {
        val transmitted: Long
        try {
            transmitted = raw.keval().roundToLong()
        } catch (e: KevalException) {
            return null
        }
        message.respond {
            content =
                "MATHE von ${message.author?.mention}\n$raw = $transmitted\nhttps://tenor.com/view/warning-alarm-warn-beware-careful-gif-10095783 "
        }
        return transmitted
    }

    private suspend fun end() {
        locked = true
        countdownEvents.remove(this)
        createEndMessage()
        removeFromDatabase()
    }

    private suspend fun createEndMessage() {
        data.channel.createMessage {
            embed {
                title = "Event abgeschlossen!"
                selfAuthor(kord)
                description = "Das Countdown-Event wurde erfolgreich abgeschlossen."
                field {
                    name = "Zeit"
                    value = DurationFormatUtils.formatDuration(
                        Duration.between(
                            data.start,
                            LocalDateTime.now()
                        ).toMillis(), "d Tage, HH:mm:ss"
                    )
                }
                field {
                    name = "Start"
                    value = data.start.format(standardDateTimeFormatter)
                }
                field {
                    name = "Ende"
                    value = LocalDateTime.now().format(standardDateTimeFormatter)
                }
                field {
                    name = "Fails"
                    value = data.fails().toString()
                }
            }
        }
    }

    private suspend fun Message.allowAndDelete() {
        allowedDeletedMessages.add(id)
        delete()
    }

    private suspend fun fail(
        failMessage: String,
        customCountdownResetMessage: String? = null,
        type: FailType,
        memberId: Long
    ) {
        data.channel.createMessage("$failMessage\n${customCountdownResetMessage ?: "Der Countdown wurde auf ${reset()} zurückgesetzt."}")
        val newFail = Pair(type, Instant.now().toEpochMilli())
        if (data.fails.containsKey(memberId)) {
            data.fails[memberId]?.add(newFail)
        } else {
            data.fails[memberId] = mutableListOf(newFail)
        }
    }

    private fun reset(): Long {
        val modulo = configData.eventData.countdownResetState.value
        var missing = modulo - (data.count % modulo)
        if (missing == modulo) {
            missing = 0
        }
        val resetValue = missing + data.count
        return (resetValue).also {
            setCount(it)
        }
    }

    private fun setCount(newCount: Long) {
        data.count = newCount
    }

    suspend fun start() {
        deleteMessages()
        sendStartMessage()
        unlock()
    }

    private suspend fun deleteMessages() {
        val bulkDelete = mutableListOf<Snowflake>()
        data.channel.asChannel().messages.toList().forEach {
            var add = false
            if (it.author?.isBot == true) {
                if (it.embeds.isEmpty()) {
                    add = true
                }
            }
            if (it.timestamp.daysUntil(
                    Clock.System.now(),
                    TimeZone.currentSystemDefault()
                ) < 14 && it.author?.isBot == false
            ) {
                add = true
            }
            if (add) {
                bulkDelete.add(it.id)
            }
        }
        allowedDeletedMessages.addAll(bulkDelete)
        data.channel.bulkDelete(bulkDelete)
        (data.channel.guild.getChannel(data.channel.id) as TextChannel).messages.onEach {
            it.allowAndDelete()
        }
    }

    fun unlock() {
        synchronized(locked) {
            locked = false
        }
    }

    private suspend fun sendStartMessage() {
        data.channel.createMessage {
            embed {
                title = "Countdown Event"
                selfAuthor(kord)
                description =
                    "Ein neues Countdown-Event wurde gestartet! Zählt bis 0 runter, um dieses Event zu schaffen. " +
                            "**Der Countdown startet bei ${data.count}**\nSollte eine Nachricht eine inkorrekte Zahl enthalten, wird der Countdown zurückgesetzt.\n\n**${data.count}**"
                timestamp = Clock.System.now()
            }
        }
    }

    private fun removeFromDatabase() {
        dataSource.connection.use { connection ->
            connection.prepareStatement("DELETE FROM event_data WHERE guild_id = ? AND channel_id = ? AND type = ?")
                .use { preparedStatement ->
                    preparedStatement.setLong(1, data.channel.guild.toLong())
                    preparedStatement.setLong(2, data.channel.toLong())
                    preparedStatement.setString(3, COUNTDOWN_EVENT_NAME)
                    preparedStatement.executeUpdate()
                }
        }
    }
}

@Serializable
data class CountdownEventData(
    @Serializable(with = TextChannelBehaviorSerializer::class) val channel: TextChannelBehavior,
    var count: Long,
    val fails: MutableMap<Long, MutableList<Pair<@Serializable(with = FailType.FailTypeSerializer::class) FailType, Long>>>,
    @Serializable(with = JavaLocalDateTimeSerializer::class) val start: LocalDateTime,
    var lastUser: Long
) {
    fun fails(): Int {
        var size = 0
        for (value in fails.values) {
            size += fails.size
        }
        return size
    }
}

@Suppress("unused")
@Serializable
enum class CountdownResetState(val value: Long, override val readableName: String) : ChoiceEnum {

    TENS_RESET_STATE(10, "Nächster Zehner"),
    HUNDREDS_RESET_STATE(100, "Nächster Hunderter"),
    THOUSANDS_RESET_STATE(1000, "Nächster Tausender"),
    TEN_THOUSANDS_RESET_STATE(10_000, "Nächster Zehntausender"),
    HUNDRED_THOUSANDS_RESET_STATE(100_100, "Nächster Hunderttausender"),
    MILLIONS_RESET_STATE(1_000_000, "Nächste Million"),
    BILLIONS_RESET_STATE(1_000_000_000, "Nächste Milliarde (ich übertreib' doch nicht)"),
    TRILLIONS_RESET_STATE(1_000_000_000_000L, "Nächste Billion (ne, echt nicht)"),
    QUADRILLIONS_RESET_STATE(1_000_000_000_000_000L, "Nächste Billiarde (...)");
}

@Serializable
open class FailType(val name: String) {

    companion object {
        val types = listOf(WrongNumber, NotANumber)
    }

    object WrongNumber : FailType("wrong_number")
    object NotANumber : FailType("not_a_number")

    class FailTypeSerializer : KSerializer<FailType> {
        override fun deserialize(decoder: Decoder): FailType {
            val name = decoder.decodeString()
            return types.firstOrNull { failType -> failType.name.equals(name, true) }
                ?: throw SerializationException("fail type for $name not found")
        }

        override val descriptor =
            PrimitiveSerialDescriptor("de.jvstvshd.foxesbot.FailTypeToStringSerializer", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: FailType) {
            TODO("Not yet implemented")
        }
    }
}

data class CheckResult(val transmitted: Long, val failMessage: String? = null, val type: FailType? = null)
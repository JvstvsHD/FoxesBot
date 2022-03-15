package de.jvstvshd.chillingfoxes.foxesbot.module.event

import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum
import com.kotlindiscord.kord.extensions.utils.dm
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
import kotlinx.coroutines.DelicateCoroutinesApi
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
import java.time.LocalDateTime

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
            message.delete()
            message.author?.dm("Du musst warten, bis jemand anderes eine Nachricht schreibt.")
            return
        }
        message.author?.toLong()?.let {
            data.lastUser = it
        }
        val content = message.content
        val transmitted = content.toLongOrNull()
        var failMessage: String? = null
        if (transmitted == null) {
            message.delete("invalid number")
            fail(
                "${message.author?.mention} hat leider keine valide Zahl abgesendet."
            )
            return
        }
        synchronized(data.count) {
            if (data.count - 1 != transmitted) {
                failMessage =
                    "${message.author?.mention} hat leider die falsche Zahl ($transmitted) abgesendet. Richtig: ${data.count - 1}"
            }
        }
        failMessage?.let {
            message.delete()
            fail(it)
            return
        }
        synchronized(data.count) {
            setCount(transmitted)
            shouldBeSaved = true
        }
        if (data.count == 0L) {
            locked = true
            countdownEvents.remove(this)
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
            removeFromDatabase()
        }
    }

    private suspend fun fail(failMessage: String, customCountdownResetMessage: String? = null) {
        data.channel.createMessage("$failMessage\n${customCountdownResetMessage ?: "Der Countdown wurde auf ${reset()} zurückgesetzt."}")
    }

    private fun reset(): Long {
        val modulo = configData.eventData.countdownResetState.value.toLong()
        var missing = modulo - (data.count % modulo)
        if (missing == modulo) {
            missing = 0;
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
        println("data = ${data.channel.messages.toList().size}")
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
        data.channel.bulkDelete(bulkDelete)
        (data.channel.guild.getChannel(data.channel.id) as TextChannel).messages.onEach {
            it.delete()
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
                            "**Der Countdown startet bei ${data.count}**\nSollte eine Nachricht eine invalide Zahl enthalten, wird der Countdown zurückgesetzt.\n\n**${data.count}**"
                timestamp = Clock.System.now()
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
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
    val fails: Map<Long, List<Pair<FailType, Long>>>,
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

@Serializable
enum class CountdownResetState(val value: Int, override val readableName: String) : ChoiceEnum {

    HUNDREDS_RESET_STATE(100, "Letzter Hunderter"),
    TENS_RESET_STATE(10, "Letzter Zehner");
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
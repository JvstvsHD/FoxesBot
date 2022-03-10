package de.jvstvshd.chillingfoxes.foxesbot.util

import com.kotlindiscord.kord.extensions.utils.getKoin
import de.jvstvshd.chillingfoxes.foxesbot.util.KordUtil.toLong
import de.jvstvshd.chillingfoxes.foxesbot.util.KordUtil.toSnowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.TextChannelBehavior
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class TextChannelBehaviorSerializer : KSerializer<TextChannelBehavior> {

    val kord: Kord = getKoin().get<Kord>()

    override fun deserialize(decoder: Decoder): TextChannelBehavior = runBlocking {
        val snowflake = decoder.decodeLong().toSnowflake()
        val channel =
            kord.getChannel(snowflake) ?: throw SerializationException("the channel $snowflake could not be found")
        if (channel !is TextChannelBehavior) {
            throw SerializationException("the channel $snowflake is not a text channel")
        }
        return@runBlocking channel
    }

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("de.jvstvshd.foxesbot.TextChannelBehaviorSerializer", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: TextChannelBehavior) {
        encoder.encodeLong(value.toLong())
    }
}
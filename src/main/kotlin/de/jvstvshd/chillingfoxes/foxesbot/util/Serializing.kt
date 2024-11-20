/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.util

import dev.kord.core.Kord
import dev.kord.core.behavior.channel.TextChannelBehavior
import dev.kordex.core.utils.getKoin
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.serializers.LocalDateTimeIso8601Serializer
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDateTime

class TextChannelBehaviorSerializer : KSerializer<TextChannelBehavior> {

    val kord: Kord = getKoin().get()

    override fun deserialize(decoder: Decoder): TextChannelBehavior = runBlocking {
        val snowflake = decoder.decodeLong().snowflake
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
        encoder.encodeLong(value.long)
    }
}

object JavaLocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override fun deserialize(decoder: Decoder): LocalDateTime =
        LocalDateTimeIso8601Serializer.deserialize(decoder).toJavaLocalDateTime()

    override val descriptor =
        PrimitiveSerialDescriptor("de.jvstvshd.foxesbot.JavaLocalDateTimeToStringSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        LocalDateTimeIso8601Serializer.serialize(encoder, value.toKotlinLocalDateTime())
    }
}
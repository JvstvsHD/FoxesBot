/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.command.converter

import de.jvstvshd.chillingfoxes.foxesbot.util.FOXES_BOT_TIME_ZONE_ID
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.StringOptionValue
import dev.kordex.core.annotations.converters.Converter
import dev.kordex.core.annotations.converters.ConverterType
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.OptionWrapper
import dev.kordex.core.commands.converters.SingleConverter
import dev.kordex.core.commands.converters.Validator
import dev.kordex.core.commands.wrapStringOption
import dev.kordex.core.i18n.types.Key
import dev.kordex.parser.StringParser
import kotlinx.datetime.*

@Converter("datetime", types = [ConverterType.DEFAULTING, ConverterType.SINGLE, ConverterType.OPTIONAL])
class DateTimeConverter(override var validator: Validator<Instant> = null) : SingleConverter<Instant>() {
    override suspend fun parse(
        parser: StringParser?,
        context: CommandContext,
        named: String?
    ): Boolean {
        val arg = named ?: parser?.parseNext()?.data ?: return false
        this.parsed = instantFromString(arg)
        return true
    }

    override val signatureType: Key
        get() = Key("converter.datetime")

    override suspend fun parseOption(
        context: CommandContext,
        option: OptionValue<*>
    ): Boolean {
        val optionValue = (option as? StringOptionValue)?.value ?: return false
        this.parsed = instantFromString(optionValue)
        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionWrapper<*> =
        wrapStringOption(arg.displayName, arg.description) { required = false }

    private fun instantFromString(string: String): Instant {
        val date: String
        val time: String
        if (string.contains(" ")) {
            val split = string.split(" ")
            date = split[0]
            time = split[1]
        } else {
            date = string
            time = "00:00:00"
        }
        return LocalDateTime(
            LocalDate.parse(date),
            LocalTime.parse(time)
        ).toInstant(TimeZone.of(FOXES_BOT_TIME_ZONE_ID))
    }
}
/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.command.converter

import de.jvstvshd.chillingfoxes.foxesbot.io.Event
import de.jvstvshd.chillingfoxes.foxesbot.io.EventTable
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
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

@Converter("event", types = [ConverterType.DEFAULTING, ConverterType.SINGLE, ConverterType.OPTIONAL])
class EventConverter(override var validator: Validator<Event> = null) : SingleConverter<Event>() {

    override val signatureType: Key
        get() = Key("converter.event")

    override suspend fun parse(
        parser: StringParser?,
        context: CommandContext,
        named: String?
    ): Boolean {
        val arg = named ?: parser?.parseNext()?.data ?: return false
        this.parsed = parseEvent(arg)
        return true
    }

    override suspend fun parseOption(
        context: CommandContext,
        option: OptionValue<*>
    ): Boolean {
        val optionValue = (option as? StringOptionValue)?.value ?: return false
        this.parsed = parseEvent(optionValue)
        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionWrapper<*> =
        wrapStringOption(arg.displayName, arg.description) { required = true }

    private suspend fun parseEvent(arg: String): Event {
        return newSuspendedTransaction { Event.find { EventTable.codeName eq arg }.first() }
    }
}
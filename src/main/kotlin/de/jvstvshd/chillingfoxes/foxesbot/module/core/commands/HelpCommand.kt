/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.core.commands


import de.jvstvshd.chillingfoxes.foxesbot.module.core.CoreModule
import de.jvstvshd.chillingfoxes.foxesbot.util.selfAuthor
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kordex.core.commands.application.slash.SlashCommand
import dev.kordex.core.commands.chat.ChatCommand
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.i18n.types.Key

suspend fun CoreModule.helpCommand() = publicSlashCommand {
    name = Key("help")
    description = Key("command.help.description")
    val kord = kord
    action {
        respond {
            val chatCommands = mutableListOf<ChatCommand<*>>()
            val slashCommands = mutableListOf<SlashCommand<*, *, *>>()
            for (value in bot.extensions.values) {
                chatCommands.addAll(value.chatCommands)
                slashCommands.addAll(value.slashCommands)
            }
            val builder = EmbedBuilder()
            builder.selfAuthor()
            builder.title = "FoxesBot - Hilfe"
            builder.url = "https://github.com/JvstvsHD/FoxesBot"
            kord.getSelf(EntitySupplyStrategy.cacheWithCachingRestFallback).avatar?.cdnUrl?.toUrl()?.let {
                builder.thumbnail {
                    url = it
                }
            }
            builder.description = "äh ja was"
            val stringBuilder = StringBuilder()
            for (slashCommand in slashCommands) {
                stringBuilder.append("**/${slashCommand.name}**: ${slashCommand.description}\n")
            }
            for (chatCommand in chatCommands) {
                println(chatCommand.extension.name)
                stringBuilder.append("**" + config.configData.baseData.prefix + chatCommand.name + "**: " + chatCommand.description + "\n")
            }
            builder.description = stringBuilder.toString()
            embeds?.add(builder)
        }
    }
}
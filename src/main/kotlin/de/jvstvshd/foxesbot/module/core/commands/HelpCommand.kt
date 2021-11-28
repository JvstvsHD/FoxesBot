package de.jvstvshd.foxesbot.module.core.commands

import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommand
import com.kotlindiscord.kord.extensions.commands.chat.ChatCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.jvstvshd.foxesbot.module.core.CoreModule
import de.jvstvshd.foxesbot.util.KordUtil
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.rest.builder.message.EmbedBuilder

suspend fun CoreModule.helpCommand() = publicSlashCommand {
    name = "help"
    description = translationsProvider.get("command.help.description", bundleName = "core")
    val kord = kord
    action {
        respond {
            val chatCommands = mutableListOf<ChatCommand<*>>()
            val slashCommands = mutableListOf<SlashCommand<*, *>>()
            for (value in bot.extensions.values) {
                chatCommands.addAll(value.chatCommands)
                slashCommands.addAll(value.slashCommands)
            }
            val builder = EmbedBuilder()
            builder.author = KordUtil.createAuthor(kord)
            builder.title = "FoxesBot - Hilfe"
            builder.url = "https://github.com/JvstvsHD/FoxesBot"
            builder.thumbnail {
                url = kord.getSelf(EntitySupplyStrategy.cacheWithCachingRestFallback).avatar?.url.toString()
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
            embeds.add(builder)
        }
    }
}
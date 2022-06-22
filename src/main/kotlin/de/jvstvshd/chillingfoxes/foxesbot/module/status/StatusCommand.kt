/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.status

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.types.respondEphemeral
import de.jvstvshd.chillingfoxes.foxesbot.io.StatusAlias
import de.jvstvshd.chillingfoxes.foxesbot.io.StatusAliasesTable
import de.jvstvshd.chillingfoxes.foxesbot.module.status.provider.StatusProvider
import de.jvstvshd.chillingfoxes.foxesbot.util.KordUtil
import dev.kord.core.Kord
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.awt.Color

class StatusArguments : Arguments() {
    val keyword by string {
        name = "type"
        description = "Typ"
    }
}

//https://www.codegrepper.com/code-examples/javascript/check+if+valid+url
private val regex = Regex(
    "^(https?://)?" + // protocol
            "((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)+[a-z]{2,}|" + // domain name
            "((\\d{1,3}\\.){3}\\d{1,3}))" + // OR ip (v4) address
            "(:\\d+)?(/[-a-z\\d%_.~+]*)*" + // port and path
            "(\\?[;&a-z\\d%_.~+=-]*)?" + // query string
            "(#[-a-z\\d_]*)?$"
)

suspend fun StatusModule.statusCommand() = publicSlashCommand(::StatusArguments) {
    name = "status"
    description = translationsProvider.get("command.status.description", bundleName = "status")
    val kord = kord
    action {
        val keyword = arguments.keyword
        newSuspendedTransaction {
            try {
                val result = StatusAlias.find { StatusAliasesTable.name eq keyword }.firstOrNull()
                val provider: StatusProvider = if (result == null) {
                    if (!regex.matches(keyword)) {
                        respondEphemeral {
                            content = "Die Anfrage ist weder voreingespeichert noch eine gültige URL."
                        }
                        return@newSuspendedTransaction
                    }
                    createProviderFromUncheckedUrl(keyword)
                } else {
                    createProvider(result.url, result.type)
                }
                val data = provider.provide()
                respond {
                    embed {
                        statusData(data, keyword, kord) {
                            translate(this)
                        }
                    }
                }
            } catch (e: Exception) {
                respond {
                    content = "Während der Bearbeitung deiner Anfrage ist ein interner Fehler aufgetreten."
                }
                e.printStackTrace()
            }
        }
    }
}

private suspend fun EmbedBuilder.statusData(
    data: StatusData,
    keyword: String,
    kord: Kord,
    translator: suspend String.() -> String
) {
    description = ""
    title = "$keyword - Status"
    url = data.url
    author = KordUtil.createAuthor(kord)
    for (mutableEntry in data.statusMap) {
        val name = mutableEntry.key
        val metaData = mutableEntry.value
        if (metaData.hasChildren()) {
            field {
                this.name = "__$name:__ " + translator(metaData.type.translationKey)
                val stringBuilder = StringBuilder()
                for (child in metaData.children) {
                    if (child.value.hasChildren()) {
                        System.err.println("Invalid value: Children has another children!")
                        stringBuilder.append("Invalid!\n")
                    }
                    stringBuilder.append("**").append(child.key).append("**: ")
                        .append(translator(child.value.type.translationKey)).append("\n")
                }
                value = stringBuilder.toString()
            }
        } else {
            description += "**$name**: ${translator(metaData.type.translationKey)}\n"
        }
    }
    color = KordUtil.convertColor(if (data.isOperational()) Color.GREEN else Color.ORANGE)
    footer = KordUtil.createFooter("Status", data.iconUrl)
    timestamp = Clock.System.now()
}
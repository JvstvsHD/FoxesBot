package de.jvstvshd.foxesbot.module.status

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.types.respondEphemeral
import com.zaxxer.hikari.HikariDataSource
import de.jvstvshd.foxesbot.module.status.provider.StatusProvider
import de.jvstvshd.foxesbot.util.KordUtil
import dev.kord.core.Kord
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import java.awt.Color
import java.util.function.Function

class StatusArguments : Arguments() {
    val keyword by string("type", "Typ")
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

suspend fun StatusModule.statusCommand(dataSource: HikariDataSource) = publicSlashCommand(::StatusArguments) {
    name = "status"
    description = translationsProvider.get("command.status.description", bundleName = "status")
    val kord = kord
    action {
        val keyword = arguments.keyword

        dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT url, type FROM status_aliases WHERE name = ?;").use { statement ->
                statement.setString(1, keyword)
                val resultSet = statement.executeQuery()
                val provider: StatusProvider
                var url: String
                if (!resultSet.next()) {
                    if (regex.matches(keyword)) {
                        try {
                            provider = createProviderFromUncheckedUrl(keyword)
                            respond {
                                embed {
                                    fillIn(keyword, keyword, kord, provider.provide(), Function {
                                        return@Function runBlocking {
                                            return@runBlocking translate(it)
                                        }
                                    }, this)
                                }
                            }
                            return@action
                        } catch (e: Exception) {
                            e.printStackTrace()
                            respondEphemeral {
                                content = translate(
                                    "command.status.noresults",
                                    keyword
                                ) + ", internal error whilst fetching url"
                            }
                            return@action
                        }
                    }
                    respondEphemeral {
                        content = translate("command.status.noresults", bundleName = "status", arrayOf(keyword))
                    }
                    return@action
                }
                try {
                    provider = createProvider(resultSet.getString(1).also { url = it }, resultSet.getString(2))
                } catch (e: Exception) {
                    respondEphemeral {
                        content = e.message
                    }
                    return@action
                }
                val data = provider.provide()
                respond {
                    embed {
                        fillIn(keyword, url, kord, data, Function {
                            return@Function runBlocking {
                                return@runBlocking translate(it)
                            }
                        }, this)
                    }
                }
            }
        }
    }
}

private suspend fun fillIn(
    keyword: String,
    url: String,
    kord: Kord?,
    data: StatusData,
    translate: Function<String, String>,
    builder: EmbedBuilder
) {
    builder.description = ""
    builder.title = "$keyword - Status"
    builder.url = url
    builder.author = KordUtil.createAuthor(kord)
    for (mutableEntry in data.statusMap) {
        val name = mutableEntry.key
        val metaData = mutableEntry.value
        if (metaData.hasChildren()) {
            builder.field {
                this.name = "__$name:__ " + translate.apply(metaData.type.translationKey)
                val stringBuilder = StringBuilder()
                for (child in metaData.children) {
                    if (child.value.hasChildren()) {
                        System.err.println("Invalid value: Children has another children!")
                        stringBuilder.append("Invalid!\n")
                    }
                    stringBuilder.append("**").append(child.key).append("**: ")
                        .append(translate.apply(child.value.type.translationKey)).append("\n")
                }
                value = stringBuilder.toString()
            }
        } else {
            builder.description = builder.description + "**$name**: ${translate.apply(metaData.type.translationKey)}\n"
        }
    }
    builder.color = KordUtil.convertColor(if (data.isOperational()) Color.GREEN else Color.ORANGE)
    builder.footer = KordUtil.createFooter("Status", data.iconUrl)
    builder.timestamp = Clock.System.now()
}

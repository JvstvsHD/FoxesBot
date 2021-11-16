package de.jvstvshd.foxesbot.module.updatetracker

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.types.respondEphemeral
import com.zaxxer.hikari.HikariDataSource
import dev.kord.common.entity.Permission
import java.sql.PreparedStatement

class SubscribeCommandArguments : Arguments() {
    val type by string("type", "Typ")
}

suspend fun UpdateTrackerModule.subscribeCommand(updateTracking: UpdateTracking, dataSource: HikariDataSource) =
    publicSlashCommand(::SubscribeCommandArguments) {
        name = "subscribe"
        description = translationsProvider.get("command.subscribe.description", bundleName = "update")
        requireBotPermissions(Permission.ManageMessages)
        action {
            println("executing")
            if (member?.id?.value?.toLong() == null) {
                System.err.println("Long ID of member $member could not be resolved.")
                respond {
                    content = translate("general.error.internal", bundleName = "general")
                }
                return@action
            }
            val id = member?.id?.value?.toLong() as Long
            println(id)
            val type = arguments.type.lowercase()
            val result = updateTracking.getUpdateTracker(type)
            if (result == null) {
                respond {
                    content = translate("command.subscribe.unknown", arrayOf(type))
                }
                return@action
            }
            dataSource.connection.use { connection ->
                connection.prepareStatement("SELECT id FROM update_tracker_subscriptions WHERE id = ? AND type = ?;")
                    .use { preparedStatement ->
                        preparedStatement.setLong(1, id)
                        preparedStatement.setString(2, type)
                        val statement: PreparedStatement
                        val remove: Boolean
                        if (preparedStatement.executeQuery().next().also { remove = it }) {
                            statement =
                                connection.prepareStatement("DELETE FROM update_tracker_subscriptions WHERE id = ?;")
                            statement.setLong(1, id)
                            result.removeUser(id)
                        } else {
                            statement =
                                connection.prepareStatement("INSERT INTO update_tracker_subscriptions (id, type) VALUES (?, ?);")
                            statement.setLong(1, id)
                            statement.setString(2, type)
                            result.addUser(id)
                        }
                        statement.executeUpdate()
                        respondEphemeral {
                            content =
                                "Du wurdest erfolgreich" + if (remove) " vom Update-Tracker $type entfernt." else " dem Update-Tracker $type hinzugefügt."
                        }
                    }
            }
        }
    }
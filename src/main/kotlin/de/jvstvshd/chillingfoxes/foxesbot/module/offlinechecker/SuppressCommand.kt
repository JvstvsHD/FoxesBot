package de.jvstvshd.chillingfoxes.foxesbot.module.offlinechecker

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalChannel
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalUser
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.jvstvshd.chillingfoxes.foxesbot.util.KordUtil.toLong
import dev.kord.common.entity.Permission

class SuppressArgs : Arguments() {
    val user by optionalUser {
        name = "user"
        description = "User"
    }
    val channel by optionalChannel {
        name = "channel"
        description = "Channel"
    }
}

suspend fun OfflineCheckerModule.suppressCommand() = publicSlashCommand(::SuppressArgs) {
    name = "suppress"
    description = "Unterdrückt das Offline-Checken bei Usern & Channeln"
    check {
        hasPermission(Permission.ManageGuild)
    }
    action {
        val type: String
        val id: Long
        if (arguments.user != null) {
            type = "member"
            id = arguments.user!!.toLong()
        } else if (arguments.channel != null) {
            type = "channel"
            id = arguments.channel!!.toLong()
        } else {
            respond {
                content = "Unzulässige Nutzung!"
            }
            return@action
        }
        dataSource.connection.use { connection ->
            connection.prepareStatement("INSERT INTO offline_checker (id, suppressed, banned, type) VALUES (?, ?, ?, ?)")
                .use {
                    it.setLong(1, id)
                    it.setBoolean(2, true)
                    it.setBoolean(3, false)
                    it.setString(4, type)
                    it.executeUpdate()
                    respond {
                        content = "Die Einstellungen wurden aktualisiert."
                    }
                    return@action
                }
        }

    }
}
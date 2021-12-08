package de.jvstvshd.foxesbot.module.core.commands

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.runSuspended
import com.zaxxer.hikari.HikariDataSource
import de.jvstvshd.foxesbot.module.core.CoreModule
import de.jvstvshd.foxesbot.module.core.music.MusicState
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Permission
import org.apache.commons.lang3.exception.ExceptionUtils

open class MusicArgs : Arguments() {
    open val name by optionalString("name", "Name des Titels")
    val url by optionalString("url", "URL/Link zum Song/Playlist")
    val topic by optionalString("thema", "Thema/Bereich/Topic des Lieds (optional)")
    val all by defaultingBoolean(
        "all",
        "alle Titel (nur bei Reaktivieren, Aktivieren & Deaktivieren)",
        defaultValue = false
    )
}

class ActivationArgs : MusicArgs() {
    val activated by defaultingBoolean("aktiviert", "aktiveren (True)/deaktivieren (False) ", true)
}

class ListArgs : Arguments() {
    val page by defaultingInt("page", "Seite (20 Elemente pro Seite)", 1)
}

@OptIn(KordVoice::class)
suspend fun CoreModule.musicCommand(commandName: String) = ephemeralSlashCommand(::MusicArgs) {
    name = commandName
    description = "Verwaltet Musik-Elemente"
    check {
        hasPermission(Permission.ManageGuild)
    }
    ephemeralSubCommand(::MusicArgs) {
        name = "add"
        description = "Fügt Titel hinzu"
        action {
            if (arguments.url == null || arguments.name == null) {
                respond {
                    content = "Beim Hinzufügen von Elementen muss eine URL angegeben werden."
                }
                return@action
            }
            try {
                addTitle(arguments.name!!, arguments.url!!, dataSource, arguments.topic ?: "general")
            } catch (e: Exception) {
                respond {
                    content = ExceptionUtils.getMessage(e)
                }
                e.printStackTrace()
                return@action
            }
            respond {
                content = "Das Element wurde erfolgreich hinzugefügt."
            }
            return@action
        }
    }
    ephemeralSubCommand(::ActivationArgs) {
        name = "aktiviert"
        description = "Setzt den Titel auf (de)aktiviert"
        action {
            val state = if (arguments.activated) MusicState.ACTIVATED else MusicState.DEACTIVATED
            if (arguments.all) {
                TODO()
            } else if (arguments.topic != null) {
                println("topic != null")
                service.changeState("topic", arguments.topic!!, state)
            } else {
                if (arguments.name == null && arguments.topic == null) {
                    respond {
                        content = "Es muss entweder ein Name ODER ein Bereich/Thema angegeben werden."
                    }
                    return@action
                }
                changeState(arguments.name!!, state, dataSource)
            }
            respond {
                content = "Der Status wurde erfolgreich geändert."
            }
            return@action
        }
    }
    ephemeralSubCommand(::MusicArgs) {
        name = "delete"
        description = "Löscht Titel ausgehend vom Bereich bzw. Thema/Namen/von der Url"
        action {
            val affectedRows = if (arguments.topic != null) {
                service.deleteByTopic(arguments.topic!!)
            } else if (arguments.url != null) {
                service.deleteByUrl(arguments.url!!)
            } else if (arguments.name != null) {
                service.deleteByUrl(arguments.name!!)
            } else {
                respond {
                    content = "Es muss mindestens ein Name, Bereich/Thema oder eine URL angegeben werden!"
                }
                return@action
            }
            respond {
                content = "Es wurden $affectedRows Elemente gelöscht."
            }
            return@action
        }
    }
    ephemeralSubCommand(::ListArgs) {
        name = "list"
        description = "Listet alle Titel auf (Seite = n; von (n - 1) · 15 bis n · 15"
        action {
            dataSource.connection.use { connection ->
                connection.prepareStatement("SELECT * FROM music WHERE ")
            }
            respond {
                content = "Coming Soon™"
            }
        }
    }
}

private suspend fun changeState(name: String, to: MusicState, dataSource: HikariDataSource) = runSuspended {
    dataSource.connection.use { connection ->
        connection.prepareStatement("UPDATE music SET state = ? WHERE name = ?").use {
            it.setString(1, to.name)
            it.setString(2, name)
            it.executeUpdate()
        }
    }
}

private suspend fun addTitle(name: String, url: String, dataSource: HikariDataSource, topic: String) = runSuspended {
    dataSource.connection.use { connection ->
        connection.prepareStatement("INSERT INTO music (name, url, state, topic) VALUES (?, ?, ?, ?)").use {
            it.setString(1, name)
            it.setString(2, url)
            it.setString(3, MusicState.ACTIVATED.name)
            it.setString(4, topic)
            it.executeUpdate()
        }
    }
}

suspend fun CoreModule.musicCommands() {
    musicCommand("music")
}
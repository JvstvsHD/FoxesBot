package de.jvstvshd.foxesbot.module.core.commands

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingInt
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.types.respondingPaginator
import com.kotlindiscord.kord.extensions.utils.runSuspended
import com.zaxxer.hikari.HikariDataSource
import de.jvstvshd.foxesbot.module.core.CoreModule
import de.jvstvshd.foxesbot.module.core.music.MusicState
import de.jvstvshd.foxesbot.module.core.music.MusicTrack
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Permission
import dev.kord.common.entity.kordLogger
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
    val topic by optionalString("thema", "Thema bzw. Bereich der abzufragenden Elemente")
}

@OptIn(KordVoice::class)
suspend fun CoreModule.musicCommand(commandName: String) = ephemeralSlashCommand(::MusicArgs) {
    name = commandName
    description = "Verwaltet Musik-Elemente"
    ephemeralSubCommand(::MusicArgs) {
        name = "add"
        description = "Fügt Titel hinzu"
        check {
            hasPermission(Permission.ManageGuild)
        }
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
        check {
            hasPermission(Permission.ManageGuild)
        }
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
        check {
            hasPermission(Permission.ManageGuild)
        }
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
    publicSubCommand(::ListArgs) {
        name = "list"
        description = "Listet alle Titel auf (Seite = n; von (n - 1) · 15 bis n · 15"
        check {
            hasPermission(Permission.ManageGuild)
        }
        action {
            val query = "SELECT * FROM music" + if (arguments.topic != null) " WHERE topic = ?" else ""
            val tracks: List<MusicTrack> = try {
                dataSource.connection.use { connection ->
                    val rs = connection.prepareStatement(query).use inner@{ ps ->
                        arguments.topic?.let {
                            ps.setString(1, it)
                        }
                        return@inner ps.executeQuery()
                    }
                    val list = mutableListOf<MusicTrack>()
                    while (rs.next()) {
                        val stateName = rs.getString(3).uppercase()
                        val state: MusicState = try {
                            MusicState.valueOf(stateName)
                        } catch (e: IllegalStateException) {
                            kordLogger.warn("mysql query returned unknown enum constant name: $stateName")
                            MusicState.UNKNOWN
                        }
                        list.add(
                            MusicTrack(
                                rs.getString(1),
                                rs.getString(2),
                                state,
                                rs.getString(4)
                            )
                        )
                    }
                    return@use list
                }
            } catch (e: Exception) {
                e.printStackTrace()
                listOf(MusicTrack("Ein Fehler ist aufgetreten.", "", MusicState.UNKNOWN, "Fehler"))
            }
            val chunked = tracks.chunked(15)
            respondingPaginator {
                for ((page, trackList) in chunked.withIndex()) {
                    page {
                        title = "Music Tracks - Seite ${page + 1}"
                        for (musicTrack in trackList) {
                            field {
                                name = musicTrack.name
                                value = "[${musicTrack.name}](${musicTrack.url}): ${musicTrack.state.readableName}"
                                inline = false
                            }
                        }
                    }
                }
            }.send()
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

private suspend fun addTitle(name: String, url: String, dataSource: HikariDataSource, topic: String) =
    runSuspended {
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
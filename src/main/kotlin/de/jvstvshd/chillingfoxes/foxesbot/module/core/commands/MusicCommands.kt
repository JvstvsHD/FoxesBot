package de.jvstvshd.chillingfoxes.foxesbot.module.core.commands

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.types.respondingPaginator
import de.jvstvshd.chillingfoxes.foxesbot.io.Music
import de.jvstvshd.chillingfoxes.foxesbot.io.MusicTable
import de.jvstvshd.chillingfoxes.foxesbot.module.core.CoreModule
import de.jvstvshd.chillingfoxes.foxesbot.module.core.music.MusicState
import de.jvstvshd.chillingfoxes.foxesbot.module.core.music.MusicTrack
import dev.kord.common.entity.Permission
import org.apache.commons.lang3.exception.ExceptionUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

open class MusicArgs : Arguments() {
    open val name by optionalString {
        name = "name"
        description = "Name des Titels"
    }
    val url by optionalString {
        name = "url"
        description = "URL/Link zum Song/Playlist"
    }
    val topic by optionalString {
        name = "thema"
        description = "Thema/Bereich/Topic des Lieds (optional)"
    }
    val all by defaultingBoolean {
        name = "all"
        description = "alle Titel (nur bei Reaktivieren, Aktivieren & Deaktivieren)"
        defaultValue = false
    }
}

class ActivationArgs : MusicArgs() {
    val activated by defaultingBoolean {
        name = "aktiviert"
        description = "aktiveren (True)/deaktivieren (False) "
        defaultValue = true
    }
}

class ListArgs : Arguments() {
    val topic by optionalString {
        name = "thema"
        description = "Thema bzw. Bereich der abzufragenden Elemente"
    }
}

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
                addTitle(arguments.name!!, arguments.url!!, arguments.topic ?: "general")
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
            val topic = arguments.topic
            if (arguments.all) {
                TODO()
            } else if (topic != null) {
                println("topic != null")
                service.changeState(state) { MusicTable.topic eq topic }
            } else {
                if (arguments.name == null && arguments.topic == null) {
                    respond {
                        content = "Es muss entweder ein Name ODER ein Bereich/Thema angegeben werden."
                    }
                    return@action
                }
                service.changeState(state) { MusicTable.name eq arguments.name!! }
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
                service.deleteByName(arguments.name!!)
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
            println("test1")
            val tracks: List<MusicTrack> = service.getMusicEntities(arguments.topic?.let { { MusicTable.topic eq it } })
                .map { MusicTrack(it.name, it.url, MusicState.valueOf(it.state.uppercase()), it.topic) }
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

private suspend fun addTitle(name: String, url: String, topic: String) = newSuspendedTransaction {
    Music.new {
        this.name = name
        this.url = url
        this.state = MusicState.ACTIVATED.name
        this.topic = topic
    }
}

suspend fun CoreModule.musicCommands() {
    musicCommand("music")
}
/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.core.commands

import de.jvstvshd.chillingfoxes.foxesbot.io.Music
import de.jvstvshd.chillingfoxes.foxesbot.io.MusicTable
import de.jvstvshd.chillingfoxes.foxesbot.module.core.CoreModule
import de.jvstvshd.chillingfoxes.foxesbot.module.core.music.MusicState
import de.jvstvshd.chillingfoxes.foxesbot.module.core.music.MusicTrack
import dev.kord.common.entity.Permission
import dev.kordex.core.checks.hasPermission
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.ephemeralSubCommand
import dev.kordex.core.commands.application.slash.publicSubCommand
import dev.kordex.core.commands.converters.impl.defaultingBoolean
import dev.kordex.core.commands.converters.impl.optionalString
import dev.kordex.core.extensions.ephemeralSlashCommand
import dev.kordex.core.i18n.toKey
import org.apache.commons.lang3.exception.ExceptionUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

open class MusicArgs : Arguments() {
    open val name by optionalString {
        name = "name".toKey()
        description = "Name des Titels".toKey()
    }
    val url by optionalString {
        name = "url".toKey()
        description = "URL/Link zum Song/Playlist".toKey()
    }
    val topic by optionalString {
        name = "thema".toKey()
        description = "Thema/Bereich/Topic des Lieds (optional)".toKey()
    }
    val all by defaultingBoolean {
        name = "alle".toKey()
        description = "alle Titel (nur bei Reaktivieren, Aktivieren & Deaktivieren)".toKey()
        defaultValue = false
    }
}

class ActivationArgs : MusicArgs() {
    val activated by defaultingBoolean {
        name = "aktiviert".toKey()
        description = "aktiveren (True)/deaktivieren (False) ".toKey()
        defaultValue = true
    }
}

class ListArgs : Arguments() {
    val topic by optionalString {
        name = "thema".toKey()
        description = "Thema bzw. Bereich der abzufragenden Elemente".toKey()
    }
}

suspend fun CoreModule.musicCommand(commandName: String) = ephemeralSlashCommand(::MusicArgs) {
    name = commandName.toKey()
    description = "Verwaltet Musik-Elemente".toKey()
    ephemeralSubCommand(::MusicArgs) {
        name = "add".toKey()
        description = "Fügt Titel hinzu".toKey()
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
        name = "aktiviert".toKey()
        description = "Setzt den Titel auf (de)aktiviert".toKey()
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
        name = "delete".toKey()
        description = "Löscht Titel ausgehend vom Bereich bzw. Thema/Namen/von der Url".toKey()
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
        name = "list".toKey()
        description = "Listet alle Titel auf (Seite = n; von (n - 1) · 15 bis n · 15".toKey()
        check {
            hasPermission(Permission.ManageGuild)
        }
        action {
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
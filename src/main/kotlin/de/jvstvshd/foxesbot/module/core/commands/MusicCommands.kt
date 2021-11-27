package de.jvstvshd.foxesbot.module.core.commands

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.enumChoice
import com.kotlindiscord.kord.extensions.commands.converters.impl.defaultingBoolean
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.runSuspended
import com.zaxxer.hikari.HikariDataSource
import de.jvstvshd.foxesbot.module.core.CoreModule
import de.jvstvshd.foxesbot.module.core.music.MusicService
import de.jvstvshd.foxesbot.module.core.music.MusicState
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Permission
import org.apache.commons.lang3.exception.ExceptionUtils

class MusicArgs : Arguments() {
    val name by string("name", "Name des Titels")
    val action by enumChoice<MusicAction>(
        "status",
        "Status",
        "typeName"
    )
    val url by optionalString("url", "URL/Link zum Song/Playlist")
    val topic by optionalString("thema", "Thema/Bereich/Topic des Lieds (optional)")
    val all by defaultingBoolean(
        "all",
        "alle Titel (nur bei Reaktivieren, Aktivieren & Deaktivieren)",
        defaultValue = false
    )

    enum class MusicAction(override val readableName: String) : ChoiceEnum {
        ACTIVATE("aktivieren"),
        ADD("hinzufügen"),
        DEACTIVATE("deaktivieren"),
        DELETE("lösche"),
        REACTIVATE("reaktivieren");
    }
}

@OptIn(KordVoice::class)
suspend fun CoreModule.musicCommand(name: String) = ephemeralSlashCommand(::MusicArgs) {
    this.name = name
    description = "Verwaltet Musik-Elemente"
    check {
        hasPermission(Permission.ManageGuild)
    }
    action {
        val elementName = arguments.name
        val service = MusicService(dataSource)
        when (val action = arguments.action) {
            MusicArgs.MusicAction.ACTIVATE, MusicArgs.MusicAction.DEACTIVATE, MusicArgs.MusicAction.DELETE -> {
                if (arguments.all) {
                    for (nameElement in service.getNames(null)) {
                        changeState(nameElement, action, dataSource)
                    }
                } else {
                    changeState(elementName, action, dataSource)
                }
                respond {
                    content = "Der Status wurde erfolgreich geändert."
                }
            }
            MusicArgs.MusicAction.REACTIVATE -> {
                println(service.reactivateAll())
                respond {
                    content = "Alle Elemente wurden reaktiviert"
                }
            }
            MusicArgs.MusicAction.ADD -> {
                if (arguments.url == null) {
                    respond {
                        content = "Beim Hinzufügen von Elementen muss eine URL angegeben werden."
                    }
                    return@action
                }
                try {
                    addTitle(elementName, arguments.url!!, dataSource, arguments.topic ?: "general")
                } catch (e: Exception) {
                    respond {
                        ephemeral = true
                        content = ExceptionUtils.getMessage(e)
                    }
                    e.printStackTrace()
                    return@action
                }
                respond {
                    content = "Das Element wurde erfolgreich hinzugefügt."
                }
            }
        }
    }
}

private suspend fun changeState(name: String, to: MusicArgs.MusicAction, dataSource: HikariDataSource) = runSuspended {
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
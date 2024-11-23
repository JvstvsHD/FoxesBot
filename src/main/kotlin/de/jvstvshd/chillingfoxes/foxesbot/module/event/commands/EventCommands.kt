/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

@file:Suppress("OPT_IN_USAGE")

package de.jvstvshd.chillingfoxes.foxesbot.module.event.commands

import de.jvstvshd.chillingfoxes.foxesbot.command.argument.MemberArguments
import de.jvstvshd.chillingfoxes.foxesbot.command.argument.OptionalMemberArguments
import de.jvstvshd.chillingfoxes.foxesbot.command.converter.defaultingDatetime
import de.jvstvshd.chillingfoxes.foxesbot.command.converter.event
import de.jvstvshd.chillingfoxes.foxesbot.command.converter.optionalDatetime
import de.jvstvshd.chillingfoxes.foxesbot.command.suggestEvents
import de.jvstvshd.chillingfoxes.foxesbot.io.*
import de.jvstvshd.chillingfoxes.foxesbot.module.event.EventModule
import de.jvstvshd.chillingfoxes.foxesbot.module.event.EventState
import de.jvstvshd.chillingfoxes.foxesbot.util.long
import de.jvstvshd.chillingfoxes.foxesbot.util.now
import dev.kord.common.Color
import dev.kord.common.entity.GuildScheduledEventEntityMetadata
import dev.kord.common.entity.GuildScheduledEventPrivacyLevel
import dev.kord.common.entity.Permission
import dev.kord.common.entity.ScheduledEntityType
import dev.kord.common.entity.optional.optional
import dev.kord.core.behavior.channel.createInvite
import dev.kord.core.behavior.createScheduledEvent
import dev.kord.core.entity.Invite
import dev.kord.core.entity.Member
import dev.kord.core.entity.VoiceState
import dev.kord.core.entity.channel.CategorizableChannel
import dev.kord.rest.builder.message.embed
import dev.kordex.core.DISCORD_GREEN
import dev.kordex.core.DISCORD_RED
import dev.kordex.core.checks.hasPermission
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.PublicSlashCommandContext
import dev.kordex.core.commands.application.slash.converters.impl.stringChoice
import dev.kordex.core.commands.application.slash.publicSubCommand
import dev.kordex.core.commands.converters.impl.*
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.i18n.toKey
import dev.kordex.core.time.TimestampType
import dev.kordex.core.time.toDiscord
import dev.kordex.core.utils.hasPermission
import dev.kordex.core.utils.suggestStringCollection
import dev.kordex.core.utils.suggestStringMap
import kotlinx.coroutines.flow.*
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlin.time.Duration.Companion.minutes

class EventCreateArguments : Arguments() {
    val name by string {
        name = "name".toKey()
        description = "Name des Event-Typen".toKey()
        validate {
            if (value.isBlank()) {
                fail("Der Name darf nicht leer sein.")
                return@validate
            }
            if (value.length > 256) {
                fail("Der Name darf nicht länger als 256 Zeichen sein.")
                return@validate
            }
            pass()
        }
    }
}

class EventStageArguments(val eventModule: EventModule) : Arguments() {
    val type by string {
        name = "type".toKey()
        description = "Typ des Events".toKey()
        autoComplete {
            suggestStringCollection(eventModule.registeredTypes)
        }
    }
    val name by string {
        name = "name".toKey()
        description = "Name des Events".toKey()
        minLength = 1
        validate {
            if (value.isBlank()) {
                fail("Der Name darf nicht leer sein.")
                return@validate
            }
            if (value.toByteArray().size > 65536) {
                fail("Der Name ist zu lang.")
                return@validate
            }
            pass()
        }
    }
    val start by defaultingDatetime {
        name = "start".toKey()
        description = "Startzeit des Events".toKey()
        defaultValue = now()
    }

    val guildEvent by defaultingBoolean {
        name = "guild-event".toKey()
        description =
            "erstellt Server-Event passend zu den hier eingegebenen Werten bei true.".toKey()
        defaultValue = false
    }
    val end by optionalDatetime {
        name = "end".toKey()
        description = "Endzeit des Events".toKey()
    }

    val location by optionalString {
        name = "location".toKey()
        description = "Ort des Events. Nur benötigt, wenn guildEvent=true".toKey()
    }

    val from by optionalString {
        name = "from".toKey()
        description = "Bestimmt, welche User direkt zu dem Event hinzugefügt werden sollen.".toKey()
        autoComplete { event ->
            suggestStringMap(
                mapOf(
                    "Alle User auf dem Server" to "all",
                    "User im selben Voice-Channel" to "channel"
                )
            )
        }
    }
}

open class EventMemberArguments : MemberArguments() {
    val event by event {
        name = "event".toKey()
        description = "Event".toKey()
        autoComplete {
            suggestEvents()
        }
    }
}

class EventParticipantArguments : EventMemberArguments() {

    val action by stringChoice {
        name = "action".toKey()
        description = "Aktion, die ausgeführt werden soll".toKey()
        choices(
            mapOf(
                "Hinzufügen" to "add",
                "Entfernen" to "remove",
                "Platzierung bearbeiten" to "edit_placement"
            ).mapKeys { entry -> entry.key.toKey() })
    }

    val placement by optionalInt {
        name = "placement".toKey()
        description = "Platzierung des Mitglieds".toKey()
    }
}

class EventViewArguments : Arguments() {
    val event by event {
        name = "event".toKey()
        description = "Event".toKey()
        autoComplete {
            suggestEvents()
        }
    }
    val member by optionalMember {
        name = "member".toKey()
        description = "Mitglied".toKey()
        validate {
            failIf(
                context.getMember()?.asMember()?.hasPermission(Permission.ManageEvents) != true,
                "Du bist nicht berechtigt, die Eventdaten anderer Spieler nachzusehen."
            )
        }
    }
}

suspend fun EventModule.eventCommand() = publicSlashCommand {
    name = "event".toKey()
    description = "Event Commands".toKey()
    allowInDms = false
    publicSubCommand(::EventCreateArguments) {
        name = "create".toKey()
        description = "Erstellt einen neuen Event-Typen".toKey()
        check {
            hasPermission(Permission.ManageEvents)
        }
        action {
            val type = newSuspendedTransaction {
                EventType.new {
                    this.name = arguments.name
                }
            }
            registeredTypes.add(type.name)
            respond {
                content = "Event-Typ ${type.name}(${type.id}) erstellt."
            }
        }
    }

    publicSubCommand(arguments = { EventStageArguments(this@eventCommand) }) {
        name = "stage".toKey()
        description = "Veranstaltet ein Event".toKey()
        check {
            hasPermission(Permission.CreateEvents)
        }
        action {
            val g = guild?.asGuild() ?: run {
                respond {
                    content = "Dieser Befehl kann nur in einem Server ausgeführt werden."
                }
                return@action
            }
            newSuspendedTransaction {
                val event = Event.new {
                    guildId = g.long
                    type = EventType.find { EventTypeTable.name eq arguments.type }.first().id
                    start = arguments.start.toJavaInstant()
                    end = arguments.end?.toJavaInstant()
                    codeName = arguments.name
                    state = if ((arguments.end ?: (now() + 5.minutes)) < now()) {
                        EventState.FINISHED
                    } else if (arguments.start < now()) {
                        EventState.ACTIVE
                    } else {
                        EventState.PENDING
                    }
                }
                if (arguments.guildEvent) {
                    handleScheduledGuildEvent()
                } else {
                    respond {
                        embed {
                            title = "Event erstellt :white_check_mark:"
                            description = eventTimeString()
                        }
                    }
                }
                val usersToAdd: Flow<Member> = when (arguments.from) {
                    "all" -> g.members.filter { it.isBot.not() }
                    "channel" -> member?.getVoiceState()?.getChannelOrNull()?.voiceStates?.map(VoiceState::getMember)
                        ?: emptyFlow()

                    else -> emptyFlow()
                }
                usersToAdd.collect { value ->
                    EventParticipant.new {
                        eventId = event.id
                        userId = value.id.long
                        guildId = g.long
                    }
                }
            }
        }
    }

    publicSubCommand(::EventParticipantArguments) {
        name = "participant".toKey()
        description = "Verwaltet Teilnehmer des Events".toKey()
        action {
            val selectedMember = arguments.member
            when (arguments.action) {
                "add" -> {
                    newSuspendedTransaction {
                        EventParticipant.new {
                            eventId = Event.find { EventTable.id eq arguments.event.id }.first().id
                            userId = selectedMember.id.long
                            guildId = guild!!.long
                            placement = arguments.placement
                        }
                    }
                    respond {
                        embed {
                            title = "Mitglied hinzugefügt"
                            color = DISCORD_GREEN
                            description = "Mitglied ${selectedMember.mention}(${selectedMember.username}) hinzugefügt."
                        }
                    }
                }

                "remove" -> {
                    val result = newSuspendedTransaction {
                        val results =
                            EventParticipant.find { EventParticipantTable.eventId eq arguments.event.id and (EventParticipantTable.userId eq selectedMember.id.long) }
                                .onEach { it.delete() }
                        if (results.empty()) {
                            respond {
                                embed {
                                    title = "Fehler"
                                    color = DISCORD_RED
                                    description =
                                        "Mitglied ${selectedMember.mention}(${selectedMember.username} ist kein Teilnehmer des Events."
                                }
                            }
                            return@newSuspendedTransaction false
                        }
                        return@newSuspendedTransaction true
                    }
                    if (result) {
                        respond {
                            embed {
                                title = "Mitglied entfernt"
                                color = DISCORD_GREEN
                                description = "Mitglied ${selectedMember.mention}(${selectedMember.username}) entfernt."
                            }
                        }
                    }
                }

                "edit_placement" -> {
                    newSuspendedTransaction {
                        val participant =
                            EventParticipant.find { EventParticipantTable.eventId eq arguments.event.id and (EventParticipantTable.userId eq selectedMember.id.long) }
                                .firstOrNull()
                        if (participant == null) {
                            respond {
                                embed {
                                    title = "Fehler"
                                    color = DISCORD_RED
                                    description =
                                        "Mitglied ${selectedMember.mention}(${selectedMember.username} ist kein Teilnehmer des Events."
                                }
                            }
                            return@newSuspendedTransaction
                        }
                        participant.placement = arguments.placement
                    }
                    respond {
                        embed {
                            title = "Platzierung geändert"
                            color = /*DISCORD_GREEN*/Color(0x00FF00)
                            description =
                                "Platzierung von ${selectedMember.mention}(${selectedMember.username}) auf ${arguments.placement} geändert."
                        }
                    }
                }
            }
        }
    }

    publicSubCommand(::EventViewArguments) {
        name = "view".toKey()
        description = "Zeigt Teilnahme-Daten über das Event für den User an.".toKey()
        action {
            val selectedMember =
                arguments.member ?: member?.asMember() ?: throw IllegalArgumentException("member is null")
            val event = arguments.event
            val participants = newSuspendedTransaction {
                EventParticipant.find { (EventParticipantTable.eventId eq event.id) and (EventParticipantTable.userId eq selectedMember.id.long) }
                    .firstOrNull()
            }
            if (participants == null) {
                respond {
                    embed {
                        title = "Fehler"
                        color = DISCORD_RED
                        description =
                            "${selectedMember.mention}(${selectedMember.username} ist kein Teilnehmer des Events."
                    }
                }
                return@action
            }
            respond {
                embed {
                    title = "Event ${event.codeName}"
                    color = DISCORD_GREEN
                    description = "Teilnahme-Daten für ${selectedMember.mention}(${selectedMember.username})"
                    val eventEnd = event.end ?: event.start
                    val hasEnd = event.end != null
                    field("Beginn") { event.start.toKotlinInstant().toDiscord(TimestampType.LongDateTime) }
                    if (hasEnd) {
                        field("Ende") {
                            event.end!!.toKotlinInstant().toDiscord(TimestampType.LongDateTime)
                        }
                    }
                    field("Platzierung") {
                        if (eventEnd.isBefore(now().toJavaInstant())) {
                            val placement = participants.placement ?: return@field "Keine Platzierung"
                            when (placement) {
                                1 -> ":first_place: Erster Platz"
                                2 -> ":second_place: Zweiter Platz"
                                3 -> ":third_place: Dritter Platz"
                                else -> "Teilgenommen"
                            }
                        } else {
                            "Das Event ist noch nicht beendet."
                        }
                    }
                }
            }
        }
    }

    publicSubCommand(::OptionalMemberArguments) {
        name = "overview".toKey()
        description = "Zeigt alle Events an, an denen der User teilnimmt.".toKey()
        action {
            val member = member?.asMember() ?: throw IllegalArgumentException("member is null")
            newSuspendedTransaction {
                val events = EventParticipant.find { EventParticipantTable.userId eq member.id.long }
                if (events.empty()) {
                    respond {
                        embed {
                            title = "Keine Events"
                            color = DISCORD_RED
                            description = "Du nimmst an keinen Events teil."
                        }
                    }
                    return@newSuspendedTransaction
                }
                respondingPaginator("default".toKey()) {
                    for (eventsPaged in events.chunked(5)) {
                        page {
                            title = "Events"
                            color = DISCORD_GREEN
                            description = "Events, an denen ${member.mention}(${member.username}) teilgenommen hat."
                            eventsPaged.forEach { eventParticipant ->
                                val event = Event.findById(eventParticipant.eventId) ?: return@forEach
                                val eventEnd = event.end ?: event.start
                                field(event.string()) {
                                    val placement = eventParticipant.placement
                                    if (eventEnd.isBefore(now().toJavaInstant())) {
                                        when (placement) {
                                            1 -> ":first_place: Erster Platz"
                                            2 -> ":second_place: Zweiter Platz"
                                            3 -> ":third_place: Dritter Platz"
                                            else -> "Teilgenommen"
                                        }
                                    } else {
                                        "Das Event ist noch nicht beendet."
                                    }
                                }
                            }
                        }
                    }
                }.send()
            }
        }
    }
}

private suspend fun PublicSlashCommandContext<EventStageArguments, ModalForm>.handleScheduledGuildEvent() {
    val eventEnd = arguments.end ?: arguments.start.plus(1.minutes)
    val event = guild!!.createScheduledEvent(
        arguments.name, GuildScheduledEventPrivacyLevel.GuildOnly, arguments.start,
        ScheduledEntityType.External
    ) {
        entityMetadata = GuildScheduledEventEntityMetadata((arguments.location ?: "Auf dem Server").optional())
        scheduledEndTime = eventEnd
    }
    val invite = scheduledGuildEventInvite()

    respond {
        content = "https://discord.gg/${invite.code}?event=${event.id}"
        embed {
            title = "Event erstellt :white_check_mark:"
            description = eventTimeString()
            url = "https://discord.gg/${invite.code}?event=${event.id}"
        }
    }
}

private fun PublicSlashCommandContext<EventStageArguments, ModalForm>.eventTimeString(): String {
    val scheduledStartTime = arguments.start
    val scheduledEndTime = arguments.end
    return if (scheduledEndTime != null) {
        "Event ${arguments.name} erstellt. Das Event geht von ${scheduledStartTime.toDiscord(TimestampType.LongDateTime)} bis ${
            scheduledEndTime.toDiscord(
                TimestampType.LongDateTime
            )
        }."
    } else {
        "Event ${arguments.name} erstellt. Das Event beginnt um ${scheduledStartTime.toDiscord(TimestampType.LongDateTime)}."
    }
}

private suspend fun PublicSlashCommandContext<EventStageArguments, ModalForm>.scheduledGuildEventInvite(): Invite {
    val eventEnd = arguments.end ?: arguments.start.plus(1.minutes)
    val permanentInvites = guild!!.invites.filter { metadata -> !metadata.temporary }
    return if ((eventEnd - now()).inWholeSeconds > 604000 && permanentInvites.count() > 0) {
        permanentInvites.first()
    } else {
        (channel.asChannel() as CategorizableChannel).createInvite {
            if (604000 < (eventEnd - now()).inWholeSeconds) {
                temporary = false
            } else {
                maxAge = eventEnd - now()
            }
        }
    }
}
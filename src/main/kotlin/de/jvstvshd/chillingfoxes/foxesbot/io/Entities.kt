/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.io

import de.jvstvshd.chillingfoxes.foxesbot.util.FOXES_BOT_TIME_ZONE_ID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class StatusAlias(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<StatusAlias>(StatusAliasesTable)

    var name by StatusAliasesTable.name
    var url by StatusAliasesTable.url
    var type by StatusAliasesTable.type

    operator fun component1() = url

    operator fun component2() = type
}

@Deprecated(message = "in favor of ChannelSettings")
class ChannelBarrier(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<ChannelBarrier>(ChannelBarriersTable)

    var name by ChannelBarriersTable.name
    var channelId by ChannelBarriersTable.channelId
    var guildId by ChannelBarriersTable.guildId
}

class Music(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<Music>(MusicTable)

    var name by MusicTable.name
    var url by MusicTable.url
    var state by MusicTable.state
    var topic by MusicTable.topic
}

class EventType(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<EventType>(EventTypeTable)

    var name by EventTypeTable.name
}

class Event(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Event>(EventTable)

    var guildId by EventTable.guildId
    var type by EventTable.type
    var start by EventTable.start
    var end by EventTable.end
    var codeName by EventTable.codeName
    var state by EventTable.state

    fun string(): String {
        val format: (Instant) -> String = {
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .format(LocalDateTime.ofInstant(it, ZoneId.of(FOXES_BOT_TIME_ZONE_ID)))
        }
        val startString = format(start)
        val endString = end?.let { format(it) }
        val timeString = if (endString != null) "$startString bis $endString" else startString
        val type = EventType.findById(type)?.name ?: "Unbekannt"
        return "$timeString: $codeName ($type)"
    }
}

class EventParticipant(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<EventParticipant>(EventParticipantTable)

    var eventId by EventParticipantTable.eventId
    var userId by EventParticipantTable.userId
    var guildId by EventParticipantTable.guildId
    var placement by EventParticipantTable.placement
}

class ChannelSettings(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<ChannelSettings>(ChannelSettingsTable)

    var guildId by ChannelSettingsTable.guildId
    var channelId by ChannelSettingsTable.channelId
    var type by ChannelSettingsTable.type
    var activated by ChannelSettingsTable.activated
    var value by ChannelSettingsTable.value
}

class MemberSettings(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<MemberSettings>(MemberSettingsTable)

    var userId by MemberSettingsTable.userId
    var guildId by MemberSettingsTable.guildId
    var type by MemberSettingsTable.type
    var active by MemberSettingsTable.active
    var value by MemberSettingsTable.value
}
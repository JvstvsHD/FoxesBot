/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.io

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class StatusAlias(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<StatusAlias>(StatusAliasesTable)

    var name by StatusAliasesTable.name
    var url by StatusAliasesTable.url
    var type by StatusAliasesTable.type

    operator fun component1() = url

    operator fun component2() = type
}

class PresenceStatus(id: EntityID<Long>) : LongEntity(id) {

    companion object : LongEntityClass<PresenceStatus>(PresenceStatusTable)

    var status by PresenceStatusTable.status
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

class EventData(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<EventData>(EventDataTable)

    var guildId by EventDataTable.guildId
    var channelId by EventDataTable.channelId
    var type by EventDataTable.type
    var data by EventDataTable.data
}

class ChannelSettings(id: EntityID<Int>) : IntEntity(id) {

    companion object : IntEntityClass<ChannelSettings>(ChannelSettingsTable)

    var guildId by ChannelSettingsTable.guildId
    var channelId by ChannelSettingsTable.channelId
    var type by ChannelSettingsTable.type
    var activated by ChannelSettingsTable.activated
    var value by ChannelSettingsTable.value
}
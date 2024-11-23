/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.io

import de.jvstvshd.chillingfoxes.foxesbot.module.event.EventState
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp

abstract class GuildIntIdTable(tableName: String, columnName: String = "id") : IntIdTable(tableName, columnName) {
    val guildId = long("guild_id")
}

open class GuildChannelIntIdTable(tableName: String, columnName: String = "id") :
    GuildIntIdTable(tableName, columnName) {
    open val channelId = long("channel_id")
}

object StatusAliasesTable : IntIdTable("status_aliases") {
    val name = varchar("name", 256)
    val url = varchar("url", 512)
    val type = varchar("type", 256)
}

@Deprecated(message = "Will be merged with ChannelSettingsTable")
object ChannelBarriersTable : GuildChannelIntIdTable("channel_barriers") {
    val name = varchar("name", 256)
}

object MusicTable : IntIdTable("music") {
    val name = varchar("name", 512)
    val url = varchar("url", 1024)
    val state = varchar("state", 64)
    val topic = varchar("topic", 256)
}

object EventTypeTable : IntIdTable("event_type") {
    val name = varchar("name", 256).uniqueIndex()
}

object EventTable : GuildIntIdTable("event") {
    val type = reference("type", EventTypeTable.id)
    val start = timestamp("start")
    val end = timestamp("end").nullable()
    val codeName = text("code_name")
    val state = enumeration<EventState>("state").default(EventState.PENDING)
}

object EventParticipantTable : IntIdTable("event_participant") {
    @Suppress("RemoveRedundantQualifierName")
    val eventId = reference("event_id", EventTable.id, onDelete = ReferenceOption.CASCADE)
    val userId = long("user_id")
    val guildId = long("guild_id")
    val placement = integer("placement").nullable()
}

object ChannelSettingsTable : GuildChannelIntIdTable("channel_settings") {
    val type = varchar("type", 128)
    val activated = bool("activated")
    val value = text("value").nullable()
}

object MemberSettingsTable : IntIdTable("member_settings") {
    val userId = long("user_id")
    val guildId = long("guild_id").nullable()
    val type = varchar("type", 128)
    val active = bool("active")
    val value = text("value").nullable()
}
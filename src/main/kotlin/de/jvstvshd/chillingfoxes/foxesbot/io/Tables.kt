/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.io

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable

open class GuildChannelIntIdTable(override val tableName: String) : IntIdTable(tableName) {
    open val channelId = long("channel_id")
    open val guildId = long("guild_id")
}

object StatusAliasesTable : IntIdTable("status_aliases") {
    val name = varchar("name", 256)
    val url = varchar("url", 512)
    val type = varchar("type", 256)
}

object PresenceStatusTable : LongIdTable("presence_status") {
    val status = varchar("status", 256)
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

object EventDataTable : GuildChannelIntIdTable("event_data") {
    val type = varchar("type", 128)
    val data = text("data")
}

object ChannelSettingsTable : GuildChannelIntIdTable("channel_settings") {
    val type = varchar("type", 128)
    val activated = bool("activated")
    val value = text("value").nullable()
}
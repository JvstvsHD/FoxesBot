package de.jvstvshd.chillingfoxes.foxesbot.io

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.LongIdTable

object StatusAliasesTable : IntIdTable("status_aliases") {
    val name = varchar("name", 256)
    val url = varchar("url", 512)
    val type = varchar("type", 256)
}

object PresenceStatusTable : LongIdTable("presence_status") {
    val status = varchar("status", 256)
}

object ChannelBarriersTable : IntIdTable("channel_barriers") {
    val name = varchar("name", 256)
    val channelId = long("channel_id")
    val guildId = long("guild_id")
}

object MusicTable : IntIdTable("music") {
    val name = varchar("name", 512)
    val url = varchar("url", 1024)
    val state = varchar("state", 64)
    val topic = varchar("topic", 256)
}
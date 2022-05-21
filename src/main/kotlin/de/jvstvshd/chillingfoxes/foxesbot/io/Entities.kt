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
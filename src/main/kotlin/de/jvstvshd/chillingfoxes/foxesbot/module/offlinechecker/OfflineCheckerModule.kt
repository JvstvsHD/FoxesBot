package de.jvstvshd.chillingfoxes.foxesbot.module.offlinechecker

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.zaxxer.hikari.HikariDataSource
import de.jvstvshd.chillingfoxes.foxesbot.config.Config
import de.jvstvshd.chillingfoxes.foxesbot.util.KordUtil.toLong
import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.channel.StageChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.user.PresenceUpdateEvent
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.core.supplier.EntitySupplyStrategy
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class OfflineCheckerModule(
    private val config: Config,
    @Deprecated(message = "Use Exposed API instead.") val dataSource: HikariDataSource
) : Extension() {

    override val name = "offline_checker"
    override val bundle = "offline_checker"
    private val offlineCheckers = mutableMapOf<Snowflake, OfflineChecker>()

    override suspend fun setup() {
        suppressCommand()
        event<VoiceStateUpdateEvent> {
            action {
                val member: Member = event.state.getMemberOrNull() ?: event.kord.getUser(
                    event.state.userId,
                    EntitySupplyStrategy.rest
                )!!.asMember(event.state.guildId)
                checkMember(member)
            }
        }
        event<ReadyEvent> {
            action {
                kord.launch {
                    delay(5.seconds)
                    check(kord)
                }
            }
        }
        event<PresenceUpdateEvent> {
            action {
                event.member.getPresenceOrNull()?.let {
                    checkMember0(event.getMember(), it.status)
                }
            }
        }
    }

    private suspend fun check(kord: Kord) {
        kord.guilds.onEach {
            checkGuild(it)
        }
    }

    private suspend fun checkGuild(guild: Guild) {
        guild.members.filter { member -> member.getVoiceStateOrNull() != null }.onEach { member ->
            if (member.getVoiceStateOrNull()?.channelId == null)
                return@onEach
            checkMember(member)
        }
    }

    private fun suppressed(member: Member, channel: Snowflake?): Boolean {
        if (channel != null) {
            dataSource.connection.use { connection ->
                connection.prepareStatement("SELECT id FROM offline_checker WHERE id = ? AND suppressed = ? AND type = ?")
                    .use {
                        it.setLong(1, channel.value.toLong())
                        it.setBoolean(2, true)
                        it.setString(3, "channel")
                        if (it.executeQuery().next()) {
                            return true
                        }
                    }
            }
        }
        dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT id FROM offline_checker WHERE id = ? AND suppressed = ? AND type = ?;")
                .use {
                    it.setLong(1, member.toLong())
                    it.setBoolean(2, true)
                    it.setString(3, "member")
                    return it.executeQuery().next()
                }
        }
    }

    private suspend fun checkMember0(member: Member, status: PresenceStatus) {
        if (status == PresenceStatus.Offline) {
            if (suppressed(member, member.getVoiceStateOrNull()?.channelId)) return
            member.getVoiceStateOrNull()?.channelId?.let {
                if (kord.getChannel(it) is StageChannel) {
                    return
                }
            }
            if (member.getVoiceStateOrNull()?.channelId == null)
                return
            getOrCreateOfflineChecker(member).start()
        }
    }

    private fun getOrCreateOfflineChecker(member: Member) =
        offlineCheckers[member.id] ?: OfflineChecker(member, config.configData, kord).also {
            offlineCheckers[member.id] = it
        }


    private suspend fun checkMember(member: Member) {
        if (member.getPresenceOrNull() == null) {
            return
        } else {
            checkMember0(member, member.getPresence().status)
        }
    }
}
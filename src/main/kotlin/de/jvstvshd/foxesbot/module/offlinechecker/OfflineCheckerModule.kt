package de.jvstvshd.foxesbot.module.offlinechecker

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.user.PresenceUpdateEvent
import dev.kord.core.event.user.VoiceStateUpdateEvent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class OfflineCheckerModule : Extension() {

    override val name = "offline_checker"
    override val bundle = "offline_checker"
    private val offlineCheckers = mutableMapOf<Snowflake, OfflineChecker>()
    private val scheduler: Scheduler = Scheduler()

    @OptIn(ExperimentalTime::class, DelicateCoroutinesApi::class)
    override suspend fun setup() {
        event<VoiceStateUpdateEvent> {
            action {
                println(event.state.data.channelId)
            }
        }
        event<ReadyEvent> {
            action {
                GlobalScope.async {
                    delay(Duration.Companion.seconds(5))
                    check(kord)
                }
            }
        }
        event<PresenceUpdateEvent> {
            action {
                if (event.presence.status == PresenceStatus.Offline && event.member.getVoiceStateOrNull()?.channelId != null) {
                    getOrCreateOfflineChecker(event.getMember()).start()
                }
            }
        }
    }

    private suspend fun check(kord: Kord) {
        kord.guilds.collect { guild ->
            checkGuild(guild)
        }
    }

    private suspend fun checkGuild(guild: Guild) {
        guild.members.filter { member -> member.getVoiceStateOrNull() != null }.collect { member ->
            if (member.getVoiceStateOrNull()?.channelId == null)
                return@collect
            checkMember(member)
        }
    }

    private fun checkMember0(member: Member, status: PresenceStatus) {
        if (status == PresenceStatus.Offline) {
            println("offline: " + member.username)
            getOrCreateOfflineChecker(member).start()
        }
    }

    private fun getOrCreateOfflineChecker(member: Member) =
        offlineCheckers[member.id] ?: OfflineChecker(member).also {
            offlineCheckers[member.id] = it
        }

    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun checkMember(member: Member) {
        println(member)
        if (member.getPresenceOrNull() == null) {
            return
        } else {
            checkMember0(member, member.getPresence().status)
        }
    }
}
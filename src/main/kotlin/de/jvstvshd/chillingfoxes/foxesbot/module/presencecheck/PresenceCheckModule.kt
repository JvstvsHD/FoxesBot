/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.presencecheck

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.kotlindiscord.kord.extensions.utils.dm
import com.kotlindiscord.kord.extensions.utils.hasRole
import de.jvstvshd.chillingfoxes.foxesbot.config.Config
import de.jvstvshd.chillingfoxes.foxesbot.io.MemberSettings
import de.jvstvshd.chillingfoxes.foxesbot.io.MemberSettingsTable
import de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.channel.ChannelFeature
import de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.channel.ChannelFeatureType
import de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.member.MemberFeature
import de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.member.MemberFeatureType
import de.jvstvshd.chillingfoxes.foxesbot.util.*
import dev.kord.common.entity.PresenceStatus
import dev.kord.core.entity.Member
import dev.kord.core.entity.Message
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.user.PresenceUpdateEvent
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.core.kordLogger
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.create.embed
import io.ktor.util.logging.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlin.time.Duration.Companion.seconds

const val SUPPRESS_PRESENCE_CHECK_TYPE = "suppress_presence_check"
const val SUPPRESS_PRESENCE_CHECK_ROLE_TYPE = "suppress_presence_check_on_role"

class PresenceCheckModule(
    private val config: Config
) : Extension() {

    companion object {
        val sessions = mutableMapOf<Member, PresenceCheckSession>()
    }

    override val name = "presence_check"
    override val bundle = "presence_check"

    override suspend fun setup() {
        kordLogger.warn("The functionality of the offline checker module is temporarily not available")
        event<VoiceStateUpdateEvent> {
            action {
                val state = event.state
                val member = state.getMember()
                if (state.channelId == null) {
                    sessions[member]?.cancel()
                    return@action
                }
                checkMember(member)
            }
        }
        event<ReadyEvent> {
            action {
                kord.launch {
                    while (isActive) {
                        kord.guilds.collect { guild ->
                            guild.members.collect { member ->
                                checkMember(member)
                            }
                        }
                        delay(config.configData.offlineCheckerData.periodInSeconds.seconds)
                    }
                }
            }
        }
        event<PresenceUpdateEvent> {
            action {
                checkMember(event.member.asMember())
            }
        }
    }

    private suspend fun checkMember(member: Member) {
        kordLogger.debug("Performing presence check for member ${member.username} (${member.id})")
        val presence = member.getPresenceOrNull()
        if (member.shouldMemberBeSkipped()) {
            return
        }
        val message = when (presence?.status) {
            null -> member.dm {
                presenceNull()
            }
            PresenceStatus.Offline, PresenceStatus.Invisible -> member.dm {
                invisible()
            }
            else -> return
        }
        if (message == null) {
            kordLogger.warn("member ${member.asString} has DMs disabled")
        }
        startNewSession(member, message)
        kordLogger.info { "presence check for member ${member.asString} failed" }
    }

    private suspend fun MessageCreateBuilder.presenceNull() {
        embed {
            selfAuthor()
            title = "Präsenz-Status Check"
            description = """
            Die Abfrage deines Präsenz-Status hat leider kein genaues Ergebnis erbracht. Es ist allerdings davon auszugehen,
            dass du auf unsichtbar gestellt bist. Sollte dies nicht der Fall sein, stelle dich doch einmal kurz auf etwas anderes (abwesend, bitte nicht stören oder online).
            Dies sollte das Problem lösen. Bekommst du trotzdem noch Nachrichten, melde dich bitte im Support.
            Solltest du deinen Präsenz-Status nicht innerhalb der nächsten 5 Minuten auf einen dieser Status setzen, wirst du vorerst von
            allen Voice-Aktivitäten ausgeschlossen.
            """.trimIndent()
            footer {
                text = "FoxesBot Präsenz-Status Check."
            }
            timestamp = Clock.System.now()
            color = Colors.RED
        }
    }

    private suspend fun MessageCreateBuilder.invisible() {
        embed {
            selfAuthor()
            title = "Präsenz-Status Check"
            description = """
                Da du dich auf unsichtbar gestellt hast, wirst du binnen der nächsten fünf Minuten vorerst von allen Voice-Aktivitäten
                 ausgeschlossen, wenn du bis dahin deinen Präsenz-Status nicht auf online/abwesend/bitte nicht stören gesetzt hast.
            """.trimIndent()
            footer {
                text =
                    "FoxesBot Präsenz-Status Check. Du erhältst diese Nachricht fehlerhaft? Melde dich im Support mit folgenden Angaben: Screenshot, möglichst genauer Zeitpunkt sowie User-ID"
            }
            timestamp = Clock.System.now()
            color = Colors.RED
        }
    }

    private suspend fun startNewSession(member: Member, message: Message?): PresenceCheckSession {
        val session =
            sessions[member] ?: PresenceCheckSession(member, config.configData, message).also { sessions[member] = it }
        session.start()
        return session
    }
}

suspend fun Member.shouldMemberBeSkipped(): Boolean {
    if (isBot) {
        return true
    }
    val channel =
        try {
            val voiceState = getVoiceStateOrNull()
            if (voiceState != null) {
                voiceState.getChannelOrNull() ?: return true
            } else {
                null
            }
        } catch (e: Exception) {
            kordLogger.error(e)
            kordLogger.info { "Skipping presence check for member $asString due to exception: $e" }
            return true
        } ?: return true
    val memberFeatures = MemberFeature.feature(this)
    if (memberFeatures.isFeatureEnabled(MemberFeatureType.SuppressPresenceCheck))
        if (newSuspendedTransaction {
                MemberSettings.find { (MemberSettingsTable.userId eq long) and (MemberSettingsTable.guildId eq guild.long) and (MemberSettingsTable.type eq SUPPRESS_PRESENCE_CHECK_TYPE) }
                    .singleOrNull() != null
            }) {
            return true
        }
    if (newSuspendedTransaction {
            MemberSettings.find { (MemberSettingsTable.type eq SUPPRESS_PRESENCE_CHECK_ROLE_TYPE) and (MemberSettingsTable.guildId eq guild.long) }
                .map { hasRole(guild.getRole(it.userId.snowflake)) }.isNotEmpty()
        }) {
        kordLogger.debug { "found role!" }
        return true
    }
    return ChannelFeature.feature(channel).isFeatureEnabled(ChannelFeatureType.SuppressPresenceCheck)
}
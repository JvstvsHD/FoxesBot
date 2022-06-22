/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.moderation

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import de.jvstvshd.chillingfoxes.foxesbot.config.Config
import de.jvstvshd.chillingfoxes.foxesbot.util.KordUtil.snowflake
import dev.kord.common.entity.AuditLogChange
import dev.kord.common.entity.AuditLogChangeKey
import dev.kord.common.entity.AuditLogEvent
import dev.kord.core.behavior.getAuditLogEntries
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.guild.BanAddEvent
import dev.kord.core.event.guild.MemberLeaveEvent
import dev.kord.core.event.guild.MemberUpdateEvent
import dev.kord.core.event.user.VoiceStateUpdateEvent
import dev.kord.core.kordLogger
import org.apache.commons.lang3.time.DurationFormatUtils
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

class ModerationModule(private val config: Config) : Extension() {

    override val name: String = "moderation"

    override suspend fun setup() {
        if (config.configData.moderationData.criminalRecordChannel <= 0 || config.configData.moderationData.criminalRecordGuild <= 0) {
            kordLogger.error("configure criminal record")
            return
        }
        event<BanAddEvent> {
            action {
                sendMessage("Ban", Duration(permanent = true), event.user, event.getBanOrNull()?.reason)
            }
        }
        event<MemberLeaveEvent> {
            action {
                event.guild.getAuditLogEntries { action = AuditLogEvent.MemberKick }.collect {
                    if (it.targetId == event.user.id) {
                        sendMessage("Kick", Duration(), event.user, it.reason)
                    }
                }
            }
        }
        event<VoiceStateUpdateEvent> {
            action {
                event.state.getGuild().getAuditLogEntries { action = AuditLogEvent.MemberDisconnect }.collect {
                    if (it.targetId == event.state.userId) {
                        sendMessage("Disconnect", Duration(), event.state.getMember().asUser(), it.reason)
                    }
                }
            }
        }
        event<MemberUpdateEvent> {
            action {
                event.guild.getAuditLogEntries { action = AuditLogEvent.MemberUpdate }.collect {
                    if (it.changes.any { auditLogChange -> auditLogChange.key == AuditLogChangeKey.CommunicationDisabledUntil }) {
                        @Suppress("UNCHECKED_CAST")
                        sendMessage(
                            "Timeout",
                            Duration(
                                java.time.Duration.between(
                                    Instant.now(),
                                    (it.changes.first { auditLogChange -> auditLogChange.key == AuditLogChangeKey.CommunicationDisabledUntil } as AuditLogChange<Instant>).new)
                                    .toKotlinDuration()),
                            event.member,
                            it.reason)
                    }
                }
            }
        }
    }

    private suspend fun sendMessage(type: String, duration: Duration?, user: User, reason: String? = null) {
        val channel = kord.getGuild(config.configData.moderationData.criminalRecordGuild.snowflake)
            ?.getChannel(config.configData.moderationData.criminalRecordChannel.snowflake)
            ?: throw IllegalArgumentException("the given configuration for a channel was invalid: the channel could not be found")
        if (channel !is GuildMessageChannel) {
            throw IllegalArgumentException("the given configuration for a channel was invalid: the channel is not a guild message channel")
        }
        channel.createMessage(
            "${user.mention}(${user.username}/${user.id})\n$type ${if (duration != null) ":" else ""}${duration?.format() ?: ""}\n${
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm:ss"))
            }\nGrund: ${reason ?: "Unbekannt"}"
        )
    }

    class Duration(val kotlinDuration: kotlin.time.Duration? = null, val permanent: Boolean = false) {
        fun format() = kotlinDuration?.let {
            DurationFormatUtils.formatDurationWords(
                kotlinDuration.inWholeMilliseconds,
                false,
                false
            )
        } ?: if (permanent) "Permanent" else "Einmalige Aktion"
    }

    fun kotlin.time.Duration.customDuration() = Duration(this)
}
/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.presencecheck

import com.kotlindiscord.kord.extensions.utils.dm
import de.jvstvshd.chillingfoxes.foxesbot.config.data.ConfigData
import de.jvstvshd.chillingfoxes.foxesbot.util.Colors
import de.jvstvshd.chillingfoxes.foxesbot.util.asString
import de.jvstvshd.chillingfoxes.foxesbot.util.selfAuthor
import dev.kord.common.entity.PresenceStatus
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Member
import dev.kord.core.entity.Message
import dev.kord.core.kordLogger
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.seconds

class PresenceCheckSession(
    private val member: Member,
    private val configData: ConfigData,
    private val originalMessage: Message?
) {

    private var job: Job? = null
    private val mutex = Mutex()

    suspend fun start() {
        if (job != null) {
            return
        }
        mutex.withLock {
            job = member.kord.launch {
                delay(configData.offlineCheckerData.timeoutInSeconds.seconds)
                if (member.shouldMemberBeSkipped() ||
                    ((member.getPresenceOrNull()?.status != PresenceStatus.Offline && member.getPresenceOrNull()?.status != PresenceStatus.Invisible) && member.getPresenceOrNull() != null)
                ) {
                    job = null
                    originalMessage?.edit {
                        embed {
                            selfAuthor()
                            title = "Präsenz-Status Check :white_check_mark:"
                            color = Colors.GREEN
                            description = "Vielen Dank!"
                            footer {
                                text =
                                    "FoxesBot Präsenz-Status Check. Du wurdest zu Unrecht gekickt? Bitte melde dich im Support mit den folgenden Angaben: Screenshot, möglichst genauer Zeitpunkt sowie User-ID"
                            }
                        }
                    }
                    PresenceCheckModule.sessions.remove(member)
                    return@launch
                }
                job = null
                kickMember()
                member.dm {
                    embed {
                        selfAuthor()
                        title = "Präsenz-Status Check"
                        color = Colors.DARK_RED
                        description =
                            "Da du deinen Präsenz-Status nicht auf online/abwesend/bitte nicht stören gesetzt hast, wurdest du vorerst von allen Voice-Aktivitäten ausgeschlossen"
                        footer {
                            text =
                                "FoxesBot Präsenz-Status Check. Du wurdest zu Unrecht gekickt? Bitte melde dich im Support mit den folgenden Angaben: Screenshot, möglichst genauer Zeitpunkt sowie User-ID"
                        }
                    }
                }
                PresenceCheckModule.sessions.remove(member)
            }
        }
    }

    private suspend fun kickMember() {
        member.edit {
            voiceChannelId = null
        }
        kordLogger.info { "kicked member ${member.asString} due to offline/invisible presence status" }
    }

    fun cancel() = job?.cancel()
}
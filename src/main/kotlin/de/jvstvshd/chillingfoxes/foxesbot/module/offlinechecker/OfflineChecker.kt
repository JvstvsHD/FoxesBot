/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.offlinechecker

import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.utils.dm
import de.jvstvshd.chillingfoxes.foxesbot.config.data.ConfigData
import de.jvstvshd.chillingfoxes.foxesbot.util.KordUtil
import dev.kord.common.entity.PresenceStatus
import dev.kord.core.Kord
import dev.kord.core.entity.Member
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import java.awt.Color
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

class OfflineChecker(
    private val member: Member,
    private var configData: ConfigData,
    private val kord: Kord
) {

    private var count = 0
    private var job: Job? = null
    private var running: Boolean = false

    private suspend fun disconnect() {
        val service = member.kord.rest.guild
        service.modifyGuildMember(member.guildId, member.id) {
            voiceChannelId = null
        }
        sendKickMessage()
    }

    @OptIn(ExperimentalTime::class)
    fun start() {
        synchronized(running) {
            if (running)
                return
        }
        job = kord.launch {
            while (job != null) {
                count++
                if (count > 2) {
                    disconnect()
                    synchronized(running) {
                        running = false
                    }
                    job?.cancel()
                    return@launch
                }
                if (!offline()) {
                    job?.cancel()
                    return@launch
                }
                sendMessage()
                delay(configData.offlineCheckerData.periodInSeconds.seconds)
            }
        }
    }

    private suspend fun offline(): Boolean = member.getPresenceOrNull()?.status == PresenceStatus.Offline

    @OptIn(ExperimentalTime::class)
    suspend fun sendMessage() {
        try {
            member.dm {
                embed {
                    author = KordUtil.createAuthor(member.kord)
                    title = "Online-Status"
                    url = "https://discord.gg/K5rhddJtyW"
                    thumbnail {
                        url =
                            member.kord.getSelf(EntitySupplyStrategy.cacheWithCachingRestFallback).avatar?.url.toString()
                    }
                    description = "Bitte setze deinen Online-Status auf etwas anderes als offline/unsichtbar!"
                    footer {
                        text =
                            "Du bist nicht auf Unsichtbar gestellt? Stelle deinen Status kurz auf etwas anderes (nur nicht unsichtbar), danach sollte das Problem behoben sein!"
                    }
                    timestamp = Clock.System.now()
                    color = DISCORD_RED
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun sendKickMessage() {
        member.dm {
            embed {
                author = KordUtil.createAuthor(member.kord)
                title = "Online-Status: Kick"
                url = "https://discord.gg/K5rhddJtyW"
                thumbnail {
                    url =
                        member.kord.getSelf(EntitySupplyStrategy.cacheWithCachingRestFallback).avatar?.url.toString()
                }
                description =
                    "Da du deinen Online-Status auch nach wiederholter Aufforderung *nicht* auf Online/Abwesend/" +
                            "Bitte nicht stören gesetzt hast, wurdest du nun vorübergehend von allen Voice-Aktivitäten" +
                            " ausgeschlossen!"
                footer {
                    text =
                        "Du bist nicht auf Unsichtbar gestellt? Stelle deinen Status kurz auf etwas anderes (nur nicht unsichtbar), danach sollte das Problem behoben sein!"
                }
                timestamp = Clock.System.now()
                color = KordUtil.convertColor(Color.RED)
            }
        }
    }

}
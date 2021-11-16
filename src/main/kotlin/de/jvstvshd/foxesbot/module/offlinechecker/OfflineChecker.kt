package de.jvstvshd.foxesbot.module.offlinechecker

import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.utils.dm
import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import de.jvstvshd.foxesbot.utils.KordUtil
import dev.kord.common.annotation.KordVoice
import dev.kord.core.entity.Member
import dev.kord.core.entity.channel.VoiceChannel
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.rest.builder.message.create.embed
import dev.kord.voice.VoiceConnection
import dev.kord.voice.VoiceConnectionBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import java.awt.Color
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class OfflineChecker(
    private val member: Member,
    private val scheduler: Scheduler,
    private val module: OfflineCheckerModule
) {

    private var count = 0
    private var job: Job? = null

    @OptIn(KordVoice::class, dev.kord.common.annotation.KordPreview::class)
    suspend fun disconnect() {
        getMemberChannel()?.let {
        }

    }

    @OptIn(KordVoice::class)
    private suspend fun buildVoiceConnection(): VoiceConnection {
        return VoiceConnectionBuilder(
            member.guild.gateway!!,
            member.kord.selfId,
            getMemberChannel()?.id!!,
            member.guildId
        ).build()
    }

    private suspend fun getMemberChannel(): VoiceChannel? {
        return member.getVoiceState().channelId?.let { member.getGuild().getChannel(it) } as VoiceChannel?
    }

    @OptIn(ExperimentalTime::class, kotlinx.coroutines.DelicateCoroutinesApi::class)
    fun start() {
        if (job != null)
            return

        job = GlobalScope.launch {
            if (module.isMemberBanned(member)) {
                disconnect()
                job?.cancel()
                return@launch
            }
            while (job != null) {
                count++
                println("count: $count")
                if (count > 2) {
                    performVoiceBan()
                    job?.cancel()
                    return@launch
                }
                sendMessage()
                delay(Duration.seconds(10))
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun sendMessage() {
        try {
            val msg = member.dm {
                embed {
                    author = KordUtil.createAuthor(member.kord)
                    title = "Online-Status"
                    url = "https://discord.gg/K5rhddJtyW"
                    thumbnail {
                        url = member.kord.getSelf(EntitySupplyStrategy.cacheWithCachingRestFallback).avatar?.url.toString()
                    }
                    description = "Bitte setzte deinen Online-Status auf etwas anderes als offline/unsichtbar!"
                    footer {
                        text =
                            "Du bist nicht auf Unsichtbar gestellt? Stelle deinen Status kurz auf etwas anderes (nur nicht Unsichtbar), danach sollte das Problem behoben sein!"
                    }
                    timestamp = Clock.System.now()
                    color = DISCORD_RED
                }
            }
            println(msg)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun performVoiceBan() {
        disconnect()
        module.voiceBan(member)
        sendKickMessage()
    }

    private suspend fun sendKickMessage() {
        member.dm {
            embed {
                author = KordUtil.createAuthor(member.kord)
                title = "Online-Status: Kick"
                url = "https://discord.gg/K5rhddJtyW"
                thumbnail {
                    url = member.kord.getSelf(EntitySupplyStrategy.cacheWithCachingRestFallback).avatar?.url.toString()
                }
                description =
                    "Da du deinen Online-Status auch nach wiederholter Aufforderung *nicht* auf Online/Abwesend/Bitte nicht stören gesetzt hast, wurdest du nun vorübergehend von allen Voice-" +
                            "Aktivitäten ausgeschlossen!"
                footer {
                    text =
                        "Du bist nicht auf Unsichtbar gestellt? Stelle deinen Status kurz auf etwas anderes (nur nicht Unsichtbar), danach sollte das Problem behoben sein!"
                }
                timestamp = Clock.System.now()
                color = KordUtil.convertColor(Color.red)
            }
        }
    }
}
package de.jvstvshd.foxesbot.module.offlinechecker

import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.utils.dm
import de.jvstvshd.foxesbot.utils.KordUtil
import dev.kord.core.entity.Member
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class OfflineChecker(
    private val member: Member
) {

    private var count = 0
    private var job: Job? = null

    @OptIn(ExperimentalTime::class, kotlinx.coroutines.DelicateCoroutinesApi::class)
    fun start() {
        if (job != null)
            return

        job = GlobalScope.launch {
            while (job != null) {
                count++
                /*if (count > 2) {
                    disconnect()
                    job?.cancel()
                    return@launch
                }*/
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

}
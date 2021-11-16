package de.jvstvshd.foxesbot.module.updatetracker.gomme

import com.zaxxer.hikari.HikariDataSource
import de.jvstvshd.foxesbot.module.updatetracker.UpdateTracker
import de.jvstvshd.foxesbot.module.updatetracker.UpdateType
import de.jvstvshd.foxesbot.module.updatetracker.gomme.provider.ChangelogProvider
import de.jvstvshd.foxesbot.module.updatetracker.gomme.provider.NewsProvider
import de.jvstvshd.foxesbot.module.updatetracker.gomme.provider.UpdateProvider
import de.jvstvshd.foxesbot.utils.KordUtil
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.runBlocking
import java.awt.Color
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class GommeUpdateTracker(
    private val users: MutableList<Long>,
    val kord: Kord?,
    private val executor: ScheduledExecutorService,
    private val dataSource: HikariDataSource
) : UpdateTracker {

    override val typeName = "gomme"
    private val icon = "https://www.gommehd.net/styles/gommehd/gommehd/motif.png"
    private val updateProviders =
        mutableListOf<UpdateProvider>(ChangelogProvider(dataSource, executor), NewsProvider(dataSource, executor))

    suspend fun start(): ScheduledFuture<*> = start(executor)

    override suspend fun start(executor: ScheduledExecutorService): ScheduledFuture<*> {
        return executor.scheduleAtFixedRate(Runnable { runBlocking { try {
            trackUpdates()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        } }, 5L, 120L, TimeUnit.SECONDS);
    }

    private suspend fun sendMessage(updateContainer: GommeUpdateContainer) {
        for (userId in users) {
            val user = kord?.getUser(Snowflake(userId), EntitySupplyStrategy.cacheWithRestFallback)
            try {
                user?.getDmChannel()?.createEmbed {
                    apply(updateContainer, this)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun apply(updateContainer: GommeUpdateContainer, builder: EmbedBuilder) {
        builder.author = KordUtil.createAuthor("GommeHD.net", updateContainer.url, icon)
        builder.title = updateContainer.title
        builder.description = ""
        builder.timestamp = kotlinx.datetime.Clock.System.now()
        builder.color = KordUtil.convertColor(Color.GREEN)
        builder.field {
            name = "URL"
            value = updateContainer.url
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun trackUpdates() {
        for (updateProvider in updateProviders) {
            val lastUrl = updateProvider.getLastUrl().get(5, TimeUnit.SECONDS)
            val newUrl = updateProvider.provide()
            if (newUrl.url != lastUrl) {
                updateProvider.setLastUrl(newUrl.url)
                val author: String = if (updateProvider is ChangelogProvider) {
                    if (updateProvider.authorName != null)
                        updateProvider.authorName as String
                    else "GommeHD.net Team"
                } else "GommeHD.net Team"
                sendMessage(
                    GommeUpdateContainer(
                        newUrl.url,
                        updateProvider.type,
                        author = author,
                        title = newUrl.title
                    )
                )
            }
        }
    }

    override fun addUser(id: Long) {
        users.add(id)
        executor.execute{
            runBlocking {
                val channel = kord?.getUser(Snowflake(id), EntitySupplyStrategy.cacheWithCachingRestFallback)?.getDmChannel()
                for (updateProvider in updateProviders) {
                    channel?.createEmbed {
                        apply(updateProvider.provide(), this)
                    }
                }
            }
        }

    }

    override fun removeUser(id: Long) {
        users.remove(id)
    }

    enum class GommeUpdateType(private val translationKey: String) : UpdateType {
        NEWS("update.gomme.news"),
        SECONDARY_NEWS("update.gomme.secondary_news"),
        CHANGELOG("update.gomme.changelog");

        override val typeName = translationKey
    }


}
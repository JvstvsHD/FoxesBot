package de.jvstvshd.foxesbot.util

import dev.kord.common.Color
import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.Entity
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.rest.builder.message.EmbedBuilder

object KordUtil {

    suspend fun createAuthor(kord: Kord?) = createAuthor(
        "FoxesBot", "https://discord.gg/K5rhddJtyW", kord?.getSelf(
            EntitySupplyStrategy.cacheWithCachingRestFallback
        )?.avatar?.url
    )

    fun createAuthor(name: String, url: String? = null, iconUrl: String? = null): EmbedBuilder.Author =
        EmbedBuilder.Author().apply { this.name = name; this.icon = iconUrl; this.url = url }

    fun convertColor(color: java.awt.Color) = Color(color.rgb)

    fun createFooter(text: String, iconUrl: String? = null) = EmbedBuilder.Footer().apply {
        this.text = text
        if (iconUrl != null) {
            this.icon = iconUrl
        }
    }

    fun Long.toSnowflake() = Snowflake(this)

    fun Entity.toLong() = id.value.toLong()

    fun String.toPresenceStatus() =
        when (this) {
            "online" -> PresenceStatus.Online
            "idle" -> PresenceStatus.Idle
            "dnd" -> PresenceStatus.DoNotDisturb
            "offline", "invisible" -> PresenceStatus.Offline
            else -> PresenceStatus.Unknown(this)
        }
}
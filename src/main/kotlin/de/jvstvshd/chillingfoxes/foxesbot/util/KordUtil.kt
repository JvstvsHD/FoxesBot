/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.util

import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.Entity
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.rest.builder.message.EmbedBuilder
import java.time.format.DateTimeFormatter

const val WEBSITE = "https://chillingfoxes.jvstvshd.de"

suspend fun EmbedBuilder.selfAuthor(kord: Kord) {
    author {
        val self = kord.getSelf()
        icon = self.avatar?.url
        name = self.username
        url = WEBSITE
    }
}

val standardDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

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

    @Deprecated(
        message = "Use property access syntax",
        replaceWith = ReplaceWith("this.snowflake"),
        level = DeprecationLevel.WARNING
    )
    fun Long.toSnowflake() = Snowflake(this)

    val Long.snowflake: Snowflake
        get() = Snowflake(this)

    fun Entity.toLong() = id.value.toLong()
}
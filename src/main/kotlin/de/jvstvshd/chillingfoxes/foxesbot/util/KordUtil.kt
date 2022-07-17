/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.util

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.utils.getKoin
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.Entity
import dev.kord.core.entity.Member
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.rest.builder.message.EmbedBuilder
import java.time.format.DateTimeFormatter

const val WEBSITE = "https://chillingfoxes.jvstvshd.de"

suspend fun EmbedBuilder.selfAuthor() {
    author {
        val self = getKoin().get<Kord>().getSelf()
        icon = self.avatar?.url
        name = self.username
        url = WEBSITE
    }
}

val standardDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

@Deprecated("refer to top-level function")
object KordUtil {

    @Suppress("DEPRECATION")
    @Deprecated("Refer to selfAuthor instead", ReplaceWith("EmbedBuilder#selfAuthor(Kord)", "dev.kord.core.Kord"))
    suspend fun createAuthor(kord: Kord?) = createAuthor(
        "FoxesBot", "https://discord.gg/K5rhddJtyW", kord?.getSelf(
            EntitySupplyStrategy.cacheWithCachingRestFallback
        )?.avatar?.url
    )

    @Deprecated("use builder instead")
    fun createAuthor(name: String, url: String? = null, iconUrl: String? = null): EmbedBuilder.Author =
        EmbedBuilder.Author().apply { this.name = name; this.icon = iconUrl; this.url = url }
}

@Deprecated(
    message = "Use property access syntax",
    replaceWith = ReplaceWith("this.snowflake"),
    level = DeprecationLevel.WARNING
)
fun Long.toSnowflake() = Snowflake(this)

val Long.snowflake: Snowflake
    get() = Snowflake(this)

@Deprecated("Use property access", ReplaceWith("this.long"))
fun Entity.toLong() = long

val Entity.long: Long
    get() = id.long

val Snowflake.long
    get() = value.toLong()

/**
 * Representation of this member in the following format: `username/id`
 */
val Member.asString
    get() = "$username/$id"

//TODO member/role settings
suspend fun CheckContext<*>.isPermitted(perm: Permission) = hasPermission(perm)
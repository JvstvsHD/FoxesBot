/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.util

import de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.member.MemberFeature
import de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.member.MemberFeatureType
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.Entity
import dev.kord.core.entity.Member
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kordex.core.annotations.NotTranslated
import dev.kordex.core.checks.hasPermission
import dev.kordex.core.checks.memberFor
import dev.kordex.core.checks.types.CheckContext
import dev.kordex.core.utils.getKoin
import java.time.format.DateTimeFormatter

const val WEBSITE = "https://github.com/JvstvsHD"

suspend fun EmbedBuilder.selfAuthor() {
    author {
        val self = getKoin().get<Kord>().getSelf()
        icon = self.avatar?.cdnUrl?.toUrl()
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
        )?.avatar?.cdnUrl?.toUrl()
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

@OptIn(NotTranslated::class)
suspend fun CheckContext<*>.isPermitted(perm: Permission) {
    val member = memberFor(event)
    if (member == null) {
        fail("Unknown member")
        return
    }
    val feature = MemberFeature.feature(member)
    if (feature.isFeatureEnabled(MemberFeatureType.BotAdmin)) {
        pass()
        return
    }
    hasPermission(perm)
}
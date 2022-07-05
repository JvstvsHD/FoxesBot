/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.util

import dev.kord.common.entity.ChannelType
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import kotlin.reflect.KProperty1

@Suppress("UNCHECKED_CAST")
fun <R> Any.instanceOf(propertyName: String): R? {
    val property = this::class.members.firstOrNull { it.name == propertyName } as KProperty1<Any, *>?
    return property?.get(this) as R?
}

fun <ID : Comparable<ID>, T : Entity<ID>> EntityClass<ID, T>.getColumn(name: String) =
    table.columns.firstOrNull { column -> column.name.equals(name, true) }

object Util {

    fun <T> executeAsync(callable: Callable<T>, executor: Executor): CompletableFuture<T> {
        val cf = CompletableFuture<T>()
        executor.execute {
            try {
                cf.complete(callable.call())
            } catch (e: Exception) {
                cf.completeExceptionally(e)
            }
        }
        return cf
    }
}

val guildChannelTypes = setOf<ChannelType>(
    ChannelType.GuildCategory,
    ChannelType.GuildDirectory,
    ChannelType.GuildNews,
    ChannelType.GuildStageVoice,
    ChannelType.GuildText,
    ChannelType.GuildVoice
)

val threadChannelTypes = setOf(ChannelType.PrivateThread, ChannelType.PublicGuildThread, ChannelType.PublicNewsThread)
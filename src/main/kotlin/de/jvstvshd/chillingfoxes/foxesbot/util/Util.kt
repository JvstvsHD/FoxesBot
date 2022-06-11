package de.jvstvshd.chillingfoxes.foxesbot.util

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

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
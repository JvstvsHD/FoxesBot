package de.jvstvshd.foxesbot.util

import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

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
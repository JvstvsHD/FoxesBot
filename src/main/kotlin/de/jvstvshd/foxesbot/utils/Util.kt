package de.jvstvshd.foxesbot.utils

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
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

    @OptIn(DelicateCoroutinesApi::class)
    fun <T> execute(callable: Callable<T>, executor: Executor): Deferred<T> {
       return GlobalScope.async {
            return@async callable.call()
        }
    }

}
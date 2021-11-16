package de.jvstvshd.foxesbot.module.updatetracker

import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture

interface UpdateTracker {

    val typeName: String

    suspend fun start(executor: ScheduledExecutorService): ScheduledFuture<*>

    fun addUser(id: Long)

    fun removeUser(id: Long)
}
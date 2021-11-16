package de.jvstvshd.foxesbot.module.updatetracker

import com.zaxxer.hikari.HikariDataSource
import java.util.concurrent.ScheduledExecutorService
import java.util.function.Supplier

class UpdateTracking(private val dataSource: HikariDataSource, private val executor: ScheduledExecutorService) {

    private val updateTrackers: MutableSet<UpdateTracker> = mutableSetOf()
    private var completedRegistrations = 0

    fun registerUpdateTracker(updateTracker: UpdateTracker) {
        if (updateTracker.typeName != updateTracker.typeName.lowercase())
            throw IllegalArgumentException("Update tracker names must only contain lowercase characters.")
        updateTrackers.add(updateTracker)
    }

    suspend fun start() {
        for (updateTracker in updateTrackers) {
            updateTracker.start(executor);
        }
    }

    fun getUpdateTracker(type: String): UpdateTracker? =
        updateTrackers.stream().filter { it.typeName == type.lowercase() }.findAny().orElseGet(Supplier {
            return@Supplier null
        })
}
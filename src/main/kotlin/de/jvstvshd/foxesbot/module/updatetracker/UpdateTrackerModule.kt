package de.jvstvshd.foxesbot.module.updatetracker

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import com.zaxxer.hikari.HikariDataSource
import de.jvstvshd.foxesbot.module.updatetracker.gomme.GommeUpdateTracker
import de.jvstvshd.foxesbot.util.Util
import dev.kord.core.event.gateway.ReadyEvent
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Suppress("SameParameterValue")
class UpdateTrackerModule(private val dataSource: HikariDataSource) : Extension() {

    override val bundle = "general"
    override val name = "update-tracker"

    private val executor = Executors.newScheduledThreadPool(10)
    private val updateTracking: UpdateTracking = UpdateTracking(executor)

    override suspend fun setup() {
        event<ReadyEvent> {
            action {
                executor.execute {
                    retrieveUsers("gomme").get(10, TimeUnit.SECONDS).apply {
                        updateTracking.registerUpdateTracker(
                            GommeUpdateTracker(
                                this.toMutableList(),
                                kord,
                                executor,
                                dataSource
                            )
                        )
                    }
                    runBlocking {
                        updateTracking.start()
                    }
                }
            }
        }
        subscribeCommand(updateTracking, dataSource)
    }

    private fun retrieveUsers(type: String): CompletableFuture<List<Long>> {
        return Util.executeAsync(Callable {
            val list = mutableListOf<Long>()
            dataSource.connection.use { connection ->
                connection.prepareStatement("SELECT id FROM update_tracker_subscriptions WHERE type = ?;")
                    .use { preparedStatement ->
                        preparedStatement.setString(1, type)
                        val resultSet = preparedStatement.executeQuery()
                        while (resultSet.next()) {
                            list.add(resultSet.getLong(1))
                        }
                    }
            }
            return@Callable list
        }, executor)
    }
}
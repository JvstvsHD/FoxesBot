package de.jvstvshd.foxesbot.module.updatetracker.gomme.provider

import com.zaxxer.hikari.HikariDataSource
import de.jvstvshd.foxesbot.module.updatetracker.gomme.GommeUpdateContainer
import de.jvstvshd.foxesbot.module.updatetracker.gomme.GommeUpdateTracker
import de.jvstvshd.foxesbot.utils.Provider
import de.jvstvshd.foxesbot.utils.Util
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

abstract class UpdateProvider(
    private val executor: Executor,
    private val dataSource: HikariDataSource,
    private val typeName: String
) : Provider<GommeUpdateContainer> {

    abstract val type: GommeUpdateTracker.GommeUpdateType

    fun setLastUrl(lastUrl: String) {
        executor.execute {
            dataSource.connection.use { connection ->
                connection.prepareStatement("INSERT INTO updates (url, type) VALUES (?, ?) ON DUPLICATE KEY UPDATE url = ?;")
                    .use { preparedStatement ->
                        preparedStatement.setString(1, lastUrl)
                        preparedStatement.setString(2, typeName)
                        preparedStatement.setString(3, lastUrl)
                        preparedStatement.executeUpdate()
                    }
            }
        }
    }

    fun getLastUrl(): CompletableFuture<String> {
        return Util.executeAsync(Callable {
            dataSource.connection.use { connection ->
                connection.prepareStatement("SELECT url FROM updates WHERE type = ?;").use { preparedStatement ->
                    preparedStatement.setString(1, typeName)
                    val rs = preparedStatement.executeQuery()
                    if (rs.next()) {
                        return@Callable rs.getString(1)
                    }
                }
            }
            return@Callable ""
        }, executor);
    }
}
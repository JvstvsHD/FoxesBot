package de.jvstvshd.chillingfoxes.foxesbot.module.status

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.zaxxer.hikari.HikariDataSource
import de.jvstvshd.chillingfoxes.foxesbot.config.Config
import de.jvstvshd.chillingfoxes.foxesbot.module.status.provider.IssueStatusProvider
import de.jvstvshd.chillingfoxes.foxesbot.module.status.provider.StatusPageProvider
import de.jvstvshd.chillingfoxes.foxesbot.module.status.provider.StatusProvider

class StatusModule(
    @Deprecated(message = "Use Exposed API instead") private val dataSource: HikariDataSource,
    private val config: Config
) : Extension() {

    override val name = "status"
    override val bundle = "status"

    override suspend fun setup() {
        statusCommand(dataSource)
    }

    fun createProvider(url: String, type: String): StatusProvider {
        return when (type) {
            "status_page" -> StatusPageProvider(url)
            "issue_status" -> IssueStatusProvider(config, url)
            else -> throw IllegalArgumentException("no provider for $type")
        }
    }

    fun createProviderFromUncheckedUrl(url: String): StatusProvider {
        return try {
            createProvider(url, "status_page")
        } catch (e: Exception) {
            println(e)
            createProvider(url, "issue_status")
        }
    }
}
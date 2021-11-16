package de.jvstvshd.foxesbot.module.updatetracker.gomme.provider

import com.zaxxer.hikari.HikariDataSource
import de.jvstvshd.foxesbot.module.updatetracker.gomme.GommeUpdateContainer
import de.jvstvshd.foxesbot.module.updatetracker.gomme.GommeUpdateTracker
import de.jvstvshd.foxesbot.utils.HttpUtil
import org.jsoup.Jsoup
import java.util.concurrent.Executor

class ChangelogProvider(private val dataSource: HikariDataSource, private val executor: Executor) : UpdateProvider(executor, dataSource, "gomme_changelog") {

    var authorName: String? = null
    val baseUrl = "https://www.gommehd.net/forum/"

    override fun provide(): GommeUpdateContainer {
        var url: String? = null
        val document = Jsoup.connect(baseUrl).get()//HttpUtil.getDocument("https://www.gommehd.net/forum/");
        for (elementsByClass in document.getElementsByClass("node-extra-title")) {
            for (element in elementsByClass.getElementsByAttributeValue("title", "Changelog")) {
                url = element.attr("href")
            }
        }
        val fullUrl = "https://gommehd.net$url"
        val changelogDocument = HttpUtil.getDocument(fullUrl)
        return GommeUpdateContainer(fullUrl, GommeUpdateTracker.GommeUpdateType.CHANGELOG, title = changelogDocument.title())
    }

    override val type = GommeUpdateTracker.GommeUpdateType.CHANGELOG
}
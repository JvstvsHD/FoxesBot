package de.jvstvshd.foxesbot.module.updatetracker.gomme.provider

import com.zaxxer.hikari.HikariDataSource
import de.jvstvshd.foxesbot.module.updatetracker.gomme.GommeUpdateContainer
import de.jvstvshd.foxesbot.module.updatetracker.gomme.GommeUpdateTracker
import de.jvstvshd.foxesbot.util.HttpUtil
import org.jsoup.Jsoup
import java.util.concurrent.Executor

class NewsProvider(private val dataSource: HikariDataSource, private val executor: Executor) : UpdateProvider(executor, dataSource, "gomme_news") {

    override fun provide(): GommeUpdateContainer {
        var url: String? = null
        val document = Jsoup.connect("https://www.gommehd.net").get()//HttpUtil.getDocument("https://www.gommehd.net");
        for (elementsByClass in document.getElementsByClass("news-referer")) {
            url = elementsByClass.attr("href")
            break
        }
        val fullUrl = "https://gommehd.net$url"
        return GommeUpdateContainer(fullUrl, GommeUpdateTracker.GommeUpdateType.NEWS, title = HttpUtil.getDocument(fullUrl).title())
    }

    override val type = GommeUpdateTracker.GommeUpdateType.NEWS
}
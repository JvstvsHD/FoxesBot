package de.jvstvshd.foxesbot.util

import org.jsoup.helper.DataUtil
import org.jsoup.nodes.Document
import java.net.URL

object HttpUtil {

    fun getDocument(url: String): Document = try {
        DataUtil.load(URL(url).openStream(), "UTF-8", url)
    } catch (e: Exception) {
        e.printStackTrace()
        throw e
    }

}
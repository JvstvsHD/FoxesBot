package de.jvstvshd.foxesbot.module.status.provider

import de.jvstvshd.foxesbot.module.status.StatusData
import de.jvstvshd.foxesbot.module.status.StatusMetaData
import de.jvstvshd.foxesbot.module.status.StatusType
import de.jvstvshd.foxesbot.utils.HttpUtil
import org.jsoup.nodes.Element
import java.net.MalformedURLException
import java.net.URL

class StatusPageProvider(private val url: String) : StatusProvider {

    private val BOT_AVATAR_URL =  "https://images-ext-1.discordapp.net/external/Uh9OrjLTA8n4df4egsz-1xq_bCBYdw9Uj2KWAxB3rzo/https/cdn.discordapp.com/avatars/863088857901826058/f6ac726b665487c2a248f3150ccbf189.webp"

    override fun provide(): StatusData {
        val document = HttpUtil.getDocument(url)
        val map = mutableMapOf<String, StatusMetaData>()
        for (elementsByClass in document.getElementsByClass("component-container")) {
            map.putAll(getStatus(elementsByClass))
        }
        val e = document.head().select("link[href~=.*\\.ico]").first()
        var url = e?.attr("href")
            ?: BOT_AVATAR_URL
        try {
            URL(url)
        } catch (e: MalformedURLException) {
            System.err.println("Malformed url: $url")
            url = BOT_AVATAR_URL
        }

        return StatusData(map, url)
    }

    private fun getStatus(element: Element): MutableMap<String, StatusMetaData> {
        val map = mutableMapOf<String, StatusMetaData>()
        val name = getName(element)
        val type = getStatusType(element)
        val children = element.getElementsByClass("child-components-container")
        for (child in children) {
            for (childContainer in child.getElementsByClass("component-inner-container")) {
                map.putAll(getStatus(childContainer))
            }
        }
        val finalMap = mutableMapOf<String, StatusMetaData>()
        val metadata = if (map.isEmpty()) StatusMetaData(name, type) else StatusMetaData(name, type, map)
        finalMap[name] = metadata
        return finalMap
    }

    private fun getName(element: Element): String {
        for (span in element.getElementsByTag("span")) {
            if (span.classNames().contains("name")) {
                return span.text()
            }
        }
        return "unknown"
    }

    private fun getStatusType(element: Element): StatusType {
        if (element.classNames().contains("component-container")) {
            for (elementsByClass in element.getElementsByAttribute("data-component-status")) {
                return StatusType.getByName(elementsByClass.attr("data-component-status"))
            }
            return StatusType.UNKNOWN
        }
        return StatusType.getByName(element.attr("data-component-status"))
    }
}
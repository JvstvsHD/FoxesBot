/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.util

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
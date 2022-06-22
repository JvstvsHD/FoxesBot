/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.status

import com.kotlindiscord.kord.extensions.extensions.Extension
import de.jvstvshd.chillingfoxes.foxesbot.config.Config
import de.jvstvshd.chillingfoxes.foxesbot.module.status.provider.IssueStatusProvider
import de.jvstvshd.chillingfoxes.foxesbot.module.status.provider.StatusPageProvider
import de.jvstvshd.chillingfoxes.foxesbot.module.status.provider.StatusProvider

class StatusModule(private val config: Config) : Extension() {

    override val name = "status"
    override val bundle = "status"

    override suspend fun setup() {
        statusCommand()
    }

    fun createProvider(url: String, type: String) =
        when (type) {
            "status_page" -> StatusPageProvider(url)
            "issue_status" -> IssueStatusProvider(config, url)
            else -> throw IllegalArgumentException("no provider for $type")
        }

    fun createProviderFromUncheckedUrl(url: String): StatusProvider {
        return try {
            createProvider(url, "status_page")
        } catch (e: Exception) {
            createProvider(url, "issue_status")
        }
    }
}
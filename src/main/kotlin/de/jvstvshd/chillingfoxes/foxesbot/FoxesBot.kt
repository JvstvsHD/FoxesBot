/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import com.kotlindiscord.kord.extensions.utils.loadModule
import de.jvstvshd.chillingfoxes.foxesbot.config.Config
import de.jvstvshd.chillingfoxes.foxesbot.config.data.ConfigData
import de.jvstvshd.chillingfoxes.foxesbot.io.setupDatabase
import de.jvstvshd.chillingfoxes.foxesbot.module.core.CoreModule
import de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.ChannelFeature
import de.jvstvshd.chillingfoxes.foxesbot.module.event.EventModule
import de.jvstvshd.chillingfoxes.foxesbot.module.offlinechecker.OfflineCheckerModule
import de.jvstvshd.chillingfoxes.foxesbot.module.status.StatusModule
import de.jvstvshd.chillingfoxes.foxesbot.util.KordUtil.snowflake
import dev.kord.common.entity.ActivityType
import dev.kord.core.Kord
import dev.kord.core.kordLogger
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import org.koin.dsl.bind
import java.io.File

class FoxesBot {

    @OptIn(PrivilegedIntent::class)
    suspend fun start() {
        val config = Config(File(System.getProperty("bot.config.file") ?: "config.json").toPath())
        config.load()
        setupDatabase(config.configData.dataBaseData)
        val configVersion = config.configData.configVersion
        if (configVersion != Config.configVersion) {
            kordLogger.info("config version update detected: $configVersion -> ${Config.configVersion}")
            config.configData.configVersion = Config.configVersion
            config.save()
        }
        val bot = ExtensibleBot(config.configData.baseData.token) {
            extensions {
                add { CoreModule(config) }
                add { StatusModule(config) }
                add { OfflineCheckerModule(config) }
                add { EventModule(config) }
            }
            applicationCommands {
                enabled = true
                register = true
                config.configData.baseData.testGuildId?.let { defaultGuild(it.snowflake) }
            }

            chatCommands {
                enabled = true
                defaultPrefix = config.configData.baseData.prefix
            }

            intents {
                +Intent.GuildMembers
                +Intent.GuildVoiceStates
                +Intent.GuildPresences
            }

            members {
                fillPresences = true
                all()
            }

            presence {
                val presence = config.configData.presenceData
                status = presence.status
                val name = presence.name
                when (presence.activityType) {
                    ActivityType.Unknown, ActivityType.Custom -> return@presence
                    ActivityType.Game -> playing(name)
                    ActivityType.Streaming -> streaming(name, presence.url!!)
                    ActivityType.Listening -> listening(name)
                    ActivityType.Watching -> watching(name)
                    ActivityType.Competing -> competing(name)
                }
            }
            i18n {
                defaultLocale = SupportedLocales.GERMAN
                localeResolver { _, _, _, _ ->
                    SupportedLocales.GERMAN
                }
            }

            hooks {
                afterKoinSetup {
                    loadModule { single { config.configData } bind ConfigData::class }
                }
                setup {
                    com.kotlindiscord.kord.extensions.utils.getKoin()
                        .get<Kord>().cache.register(ChannelFeature.dataDescription)
                }
            }

            kord {
                defaultStrategy = EntitySupplyStrategy.cacheWithCachingRestFallback
                enableShutdownHook = true
            }
        }
        bot.start()
    }
}
/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot

import de.jvstvshd.chillingfoxes.foxesbot.config.Config
import de.jvstvshd.chillingfoxes.foxesbot.config.data.ConfigData
import de.jvstvshd.chillingfoxes.foxesbot.io.setupDatabase
import de.jvstvshd.chillingfoxes.foxesbot.module.core.CoreModule
import de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.channel.ChannelFeature
import de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.member.MemberFeature
import de.jvstvshd.chillingfoxes.foxesbot.module.event.EventModule
import de.jvstvshd.chillingfoxes.foxesbot.module.status.StatusModule
import de.jvstvshd.chillingfoxes.foxesbot.util.snowflake
import dev.kord.common.entity.ActivityType
import dev.kord.core.Kord
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import dev.kordex.core.ExtensibleBot
import dev.kordex.core.i18n.SupportedLocales
import dev.kordex.core.utils.getKoin
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.dsl.bind
import org.koin.dsl.module
import java.io.File

val logger = KotlinLogging.logger("FoxesBot")

class FoxesBot {

    @OptIn(PrivilegedIntent::class)
    suspend fun start() {
        val config = Config(File(System.getProperty("bot.config.file") ?: "config.json").toPath())
        config.load()
        setupDatabase(config.configData.dataBaseData)
        val configVersion = config.configData.configVersion
        logger.debug { "loaded configuration (version $configVersion)" }
        if (configVersion != Config.configVersion) {
            logger.info { "config version update detected: $configVersion -> ${Config.configVersion}" }
            config.configData.configVersion = Config.configVersion
            config.save()
        }
        val bot = ExtensibleBot(config.configData.baseData.token) {
            extensions {
                add { CoreModule(config) }
                add { StatusModule(config) }
                //add { PresenceCheckModule(config) }
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
                    is ActivityType.Unknown, ActivityType.Custom -> return@presence
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
                    val module = module { single { config.configData } bind ConfigData::class }
                    getKoin().loadModules(listOf(module))
                }
                setup {
                    val cache = getKoin().get<Kord>().cache
                    cache.register(ChannelFeature.dataDescription, MemberFeature.dataDescription)
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
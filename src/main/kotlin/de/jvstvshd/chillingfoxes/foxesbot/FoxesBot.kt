package de.jvstvshd.chillingfoxes.foxesbot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import com.kotlindiscord.kord.extensions.utils.loadModule
import de.jvstvshd.chillingfoxes.foxesbot.config.Config
import de.jvstvshd.chillingfoxes.foxesbot.config.data.ConfigData
import de.jvstvshd.chillingfoxes.foxesbot.io.Database
import de.jvstvshd.chillingfoxes.foxesbot.io.setupDatabase
import de.jvstvshd.chillingfoxes.foxesbot.module.core.CoreModule
import de.jvstvshd.chillingfoxes.foxesbot.module.event.EventModule
import de.jvstvshd.chillingfoxes.foxesbot.module.offlinechecker.OfflineCheckerModule
import de.jvstvshd.chillingfoxes.foxesbot.module.status.StatusModule
import de.jvstvshd.chillingfoxes.foxesbot.util.KordUtil.snowflake
import dev.kord.common.entity.PresenceStatus
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
        val database = Database(config.configData.dataBaseData)

        val bot = ExtensibleBot(config.configData.baseData.token) {
            extensions {
                add { CoreModule(config) }
                add { StatusModule(database.dataSource, config) }
                add { OfflineCheckerModule(config, database.dataSource) }
                add { EventModule(database.dataSource, config) }
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
                status = PresenceStatus.Online
                playing("mit neuen Ideen & deinem Leben")
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
            }
            kord {
                defaultStrategy = EntitySupplyStrategy.cacheWithCachingRestFallback
                enableShutdownHook = true
            }
        }
        bot.start()
    }
}
package de.jvstvshd.foxesbot

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import de.jvstvshd.foxesbot.config.Config
import de.jvstvshd.foxesbot.io.Database
import de.jvstvshd.foxesbot.module.christmas.ChristmasModule
import de.jvstvshd.foxesbot.module.core.CoreModule
import de.jvstvshd.foxesbot.module.offlinechecker.OfflineCheckerModule
import de.jvstvshd.foxesbot.module.status.StatusModule
import de.jvstvshd.foxesbot.module.updatetracker.UpdateTrackerModule
import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import dev.kord.gateway.Intent
import dev.kord.gateway.PrivilegedIntent
import java.io.File
import java.util.concurrent.Executors

class FoxesBot {

    @OptIn(PrivilegedIntent::class)
    suspend fun start() {
        val config = Config(File(System.getProperty("bot.config.file") ?: "config.json").toPath())
        config.load()
        val database = Database(config.configData.dataBaseData)
        val datasource = database.dataSource

        val bot = ExtensibleBot(config.configData.baseData.token) {
            extensions {
                add { CoreModule(config, datasource) }
                add { UpdateTrackerModule(datasource) }
                add { StatusModule(datasource, config) }
                add { OfflineCheckerModule(datasource, Executors.newScheduledThreadPool(10)) }
                add { ChristmasModule(Executors.newScheduledThreadPool(10), datasource) }
            }

            applicationCommands {
                enabled = true
                register = true
                defaultGuild(Snowflake(config.configData.baseData.testGuildId))
            }

            chatCommands {
                enabled = true
                defaultPrefix = config.configData.baseData.prefix
            }

            intents {
                /*+Intent.GuildMembers
                +Intent.GuildVoiceStates*/
                +Intent.GuildPresences
            }

            members {
                fillPresences = true
            }

            presence {
                status = PresenceStatus.Online
                playing("/help")
            }
            i18n {
                defaultLocale = SupportedLocales.GERMAN
                localeResolver { guild, channel, user ->
                    SupportedLocales.GERMAN
                }
            }
        }
        bot.start()
    }
}

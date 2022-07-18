/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.core.settings

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.EphemeralSlashCommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.commands.converters.impl.member
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.commands.converters.impl.string
import com.kotlindiscord.kord.extensions.components.buttons.PublicInteractionButtonContext
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.publicButton
import com.kotlindiscord.kord.extensions.components.publicSelectMenu
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.types.respondEphemeral
import de.jvstvshd.chillingfoxes.foxesbot.module.core.CoreModule
import de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.channel.ChannelFeature
import de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.channel.ChannelFeatureData
import de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.channel.ChannelFeatureType
import de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.member.MemberFeature
import de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.member.MemberFeatureType
import de.jvstvshd.chillingfoxes.foxesbot.util.*
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.delegate.delegate
import dev.kord.core.behavior.MemberBehavior
import dev.kord.core.behavior.channel.GuildChannelBehavior
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.followup.edit
import dev.kord.core.entity.KordEntity
import dev.kord.core.entity.Message
import dev.kord.core.kordLogger
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.apache.commons.lang3.StringUtils
import org.intellij.lang.annotations.Language
import javax.script.ScriptEngineManager
import kotlin.io.path.Path
import kotlin.io.path.readLines
import kotlin.time.Duration.Companion.minutes

const val ENABLE_BUTTON = "enable_button"
const val DISABLE_BUTTON = "disable_button"

class ChannelSettingsArguments : Arguments() {
    val channel by channel {
        name = "channel"
        description = "Der Channel, dessen Einstellungen geändert werden sollen"
        requiredChannelTypes = (guildChannelTypes + threadChannelTypes).toMutableSet()
        requireSameGuild = true
    }
}

class MemberSettingsArguments : Arguments() {
    val member by member {
        name = "member"
        description = "Member, dessen Einstellungen geändert werden sollen"
    }
}

class ExtraSettingsArguments : Arguments() {
    val extra by string {
        name = "extra"
        description = "Extra, welches ausgeführt werden soll"
    }
}

class EvalArguments : Arguments() {
    val script by optionalString {
        name = "script"
        description = "Pfad zum Skript, welches ausgeführt werden soll"
    }
    val code by optionalString {
        name = "code"
        description = "Auszuführender Code"
    }
}

suspend fun CoreModule.settingsCommand() = publicSlashCommand {
    name = "settings"
    description = "Einstellungen"
    check {
        hasPermission(Permission.ManageGuild)
    }
    channelSettingsSubCommand()
    memberSettingsSubCommand()
    ephemeralSubCommand(::ExtraSettingsArguments) {
        name = "extra"
        description =
            "Extras zum Ausführen von Zusatzfunktionen."
        check {
            isPermitted(Permission.ManageGuild)
        }
        action {
            confirmation {
                val result: String =
                    when (arguments.extra) {
                        "saveConfig" -> {
                            config.save()
                            "saved"
                        }
                        "loadConfig" -> {
                            config.load()
                            "loaded"
                        }
                        "reloadConfig" -> {
                            config.save()
                            config.load()
                            "reloaded"
                        }
                        else -> "Unbekanntes Extra"
                    }
                it.edit {
                    content = result
                }
            }
        }
    }
    ephemeralSubCommand(::EvalArguments) {
        name = "eval"
        description = "Ausführen von Code"
        check {
            isPermitted(Permission.ManageGuild)
        }
        action {
            val script = arguments.script
            val code = arguments.code
            confirmation {
                it.edit {
                    content = "Code wird kompiliert und ausgeführt..."
                }
                val scriptContent = if (script != null) {
                    Path(script).readLines().joinToString("\n")
                } else if (code != null) {
                    code
                } else {
                    it.edit {
                        content = "Unbekannte Option!"
                    }
                    return@confirmation
                }
                for (engineFactory in ScriptEngineManager().engineFactories) {
                    kordLogger.info { engineFactory.engineName }
                }
                val scriptEngine = ScriptEngineManager().getEngineByName("kotlin")
                scriptEngine.eval(
                    """
                    import de.jvstvshd.chillingfoxes.foxesbot.*
                    import de.jvstvshd.chillingfoxes.foxesbot.config.*
                    import de.jvstvshd.chillingfoxes.foxesbot.config.data.*
                    import de.jvstvshd.chillingfoxes.foxesbot.io.*
                    import de.jvstvshd.chillingfoxes.foxesbot.module.core.*
                    import de.jvstvshd.chillingfoxes.foxesbot.module.event.*
                    import de.jvstvshd.chillingfoxes.foxesbot.module.moderation.*
                    import de.jvstvshd.chillingfoxes.foxesbot.module.presencecheck.*
                    import de.jvstvshd.chillingfoxes.foxesbot.module.status.*
                    import org.jetbrains.exposed.sql.transactions.*
                    import dev.kord.common.entity.*
                    import kotlinx.coroutines.runBlocking
                """.trimIndent()
                )
                scriptEngine.put("context", this)
                @Language("kotlin")
                val suspendingScript = """
                runBlocking {
                    $scriptContent
                }
            """.trimIndent()
                val evaluation = try {
                    scriptEngine.eval(suspendingScript)?.toString() ?: "null"
                } catch (e: Exception) {
                    it.edit {
                        content = StringUtils.truncate(e.stackTraceToString(), 2000)
                    }
                    return@confirmation
                }
                val result = if (evaluation.length > EmbedBuilder.Limits.description - "Ausgabe: ``````".length) {
                    StringUtils.truncate(evaluation, EmbedBuilder.Limits.description)
                } else {
                    "Ausgabe: ```$evaluation```"
                }
                it.edit {
                    embed {
                        selfAuthor()
                        title = "Ausführung des Skripts"
                        description = result
                        timestamp = Clock.System.now()
                    }
                }
            }
        }
    }
}

private suspend fun <A : Arguments> EphemeralSlashCommandContext<A>.confirmation(block: suspend EphemeralSlashCommandContext<A>.(Message) -> Unit) {
    respond {
        embed {
            selfAuthor()
            title = "Bestätigung"
            description = """
                Bist du sicher, dass du fortfahren willst? **Mit dem Bestätigen dieser Nachricht willigst du ein, 
                für jegliche entstehende Schäden die Verantwortung zu übernehmen und ggf. Konsequenzen zu tragen.**
            """.trimIndent()
            footer {
                text = "Sicherheitsabfrage"
            }
            timestamp = now()
        }
        components(1.minutes) {
            publicButton {
                style = ButtonStyle.Success
                label = "Bestätigen"
                id = "eval_confirmation_confirm"
                action {
                    block(this@confirmation, message)
                }
            }

            publicButton {
                style = ButtonStyle.Danger
                label = "Abbrechen"
                id = "eval_confirmation_cancel"
                action {
                    message.delete()
                }
            }
        }
    }
}

context(CoreModule)
        private suspend fun PublicSlashCommand<*>.channelSettingsSubCommand() =
    entitySettingsSubCommand<ChannelSettingsArguments, GuildChannelBehavior, ChannelFeatureType<out ChannelFeatureData>, ChannelFeature>(
        ::ChannelSettingsArguments
    ) {
        commandName = "channel"
        cmdDescription = "Channel Einstellungen"
        featureBuilder = {
            ChannelFeature.feature(this)
        }
        entityGetter = {
            guild!!.getChannel(arguments.channel.id)
        }
        entityName = {
            "Channel ${asChannel().data.name.value ?: "unbekannt"}"
        }
        featureFromString = {
            ChannelFeatureType.fromStringOrElseThrow(this)
        }
        features.addAll(ChannelFeatureType.features)
    }

context (CoreModule)
        private suspend fun PublicSlashCommand<*>.memberSettingsSubCommand() =
    entitySettingsSubCommand<MemberSettingsArguments, MemberBehavior, MemberFeatureType<out EntityFeatureData<MemberBehavior>>, MemberFeature>(
        ::MemberSettingsArguments
    ) {
        commandName = "member"
        cmdDescription = "Member Einstellungen"
        featureBuilder = {
            MemberFeature.feature(this)
        }
        entityGetter = {
            arguments.member
        }
        entityName = {
            "Member ${asMember().username}"
        }
        featureFromString = {
            MemberFeatureType.fromStringOrElseThrow(this)
        }
        features.addAll(MemberFeatureType.features)
    }

context(CoreModule)
        private suspend fun <T : Arguments, ENTITY : KordEntity, TYPE : EntityFeatureType<EntityFeatureData<ENTITY>, ENTITY>, FEATURE : EntityFeature<ENTITY, TYPE>>
        PublicSlashCommand<*>.entitySettingsSubCommand(
    arguments: () -> T,
    block: EntitySettingsSubCommandBuilder<T, ENTITY, TYPE, FEATURE>.() -> Unit
) =
    ephemeralSubCommand(arguments) {
        val builder = EntitySettingsSubCommandBuilder<T, ENTITY, TYPE, FEATURE>().apply(block)
        with(builder) {
            name = commandName!!
            description = cmdDescription!!
            check {
                isPermitted(Permission.ManageGuild)
            }
            action {
                val entity = entityGetter!!(this)
                val message = respond {
                    embed {
                        title = "Einstellungen/Features für ${entityName!!(entity) ?: "?"}"
                        description = "Loading..."
                    }
                }
                this@ephemeralSubCommand.kord.launch {
                    val entityFeature = featureBuilder!!(entity)
                    message.edit {
                        embed {
                            title = "Einstellungen/Features für ${entityName!!(entity) ?: "?"}"
                            selfAuthor()
                            description = entity.instanceOf("mention") ?: "angefordert von ${member?.mention}"
                            for ((feature, active) in entityFeature.features) {
                                val emoji = (if (active) ":green" else ":red") + "_square:"
                                field {
                                    name = "$emoji ${feature.name}"
                                    value = feature.description
                                }
                            }
                        }
                        components(2.minutes) {
                            var selectedFeature: TYPE? = null
                            publicButton {
                                deferredAck = true
                                label = "Aktivieren"
                                style = ButtonStyle.Success
                                id = ENABLE_BUTTON
                                action inner@{
                                    entityFeature.changeState(selectedFeature, true)
                                }
                            }
                            publicButton {
                                label = "Deaktivieren"
                                style = ButtonStyle.Danger
                                id = DISABLE_BUTTON
                                action {
                                    entityFeature.changeState(selectedFeature, false)
                                }
                            }

                            publicSelectMenu {
                                for (feature in features) {
                                    option(feature.name, feature.id) { deferredAck = true }
                                }
                                minimumChoices = 1
                                maximumChoices = 1
                                action {
                                    selectedFeature = featureFromString!!(selected.first())
                                }
                            }
                        }
                    }
                }
            }
        }
    }

context(PublicInteractionButtonContext)
        suspend fun <ENTITY : KordEntity, TYPE : EntityFeatureType<EntityFeatureData<ENTITY>, ENTITY>, FEATURE : EntityFeature<ENTITY, TYPE>> FEATURE.changeState(
    feature: TYPE?,
    to: Boolean
) {
    if (feature == null) {
        respondEphemeral {
            content = "bitte wähle eine Option!"
        }
        return
    }
    features[feature] = to
    update()
}

class EntitySettingsSubCommandBuilder<A : Arguments, ENTITY : KordEntity, TYPE : EntityFeatureType<EntityFeatureData<ENTITY>, ENTITY>, FEATURE : EntityFeature<ENTITY, TYPE>> {

    private var _commandName: Optional<String> = Optional.Missing()
    var commandName by ::_commandName.delegate()

    private var _cmdDescription: Optional<String> = Optional.Missing()
    var cmdDescription by ::_cmdDescription.delegate()

    private var _featureBuilder: Optional<suspend ENTITY.() -> FEATURE> = Optional.Missing()
    var featureBuilder: (suspend ENTITY.() -> FEATURE)? by ::_featureBuilder.delegate()

    private var _entityGetter: Optional<suspend EphemeralSlashCommandContext<A>.() -> ENTITY> = Optional.Missing()
    var entityGetter by ::_entityGetter.delegate()

    private var _entityName: Optional<suspend ENTITY.() -> String?> = Optional.Missing()
    var entityName by ::_entityName.delegate()

    private var _featureFromString: Optional<String.() -> TYPE> = Optional.Missing()
    var featureFromString by ::_featureFromString.delegate()

    private var _features: MutableList<TYPE> = mutableListOf()
    var features by ::_features
}
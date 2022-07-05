/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.core.commands

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.PublicSlashCommand
import com.kotlindiscord.kord.extensions.commands.application.slash.ephemeralSubCommand
import com.kotlindiscord.kord.extensions.commands.converters.impl.channel
import com.kotlindiscord.kord.extensions.components.buttons.PublicInteractionButtonContext
import com.kotlindiscord.kord.extensions.components.components
import com.kotlindiscord.kord.extensions.components.publicButton
import com.kotlindiscord.kord.extensions.components.publicSelectMenu
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.types.respondEphemeral
import de.jvstvshd.chillingfoxes.foxesbot.module.core.CoreModule
import de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.ChannelFeature
import de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.ChannelFeatureType
import de.jvstvshd.chillingfoxes.foxesbot.util.guildChannelTypes
import de.jvstvshd.chillingfoxes.foxesbot.util.selfAuthor
import de.jvstvshd.chillingfoxes.foxesbot.util.threadChannelTypes
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.interaction.followup.edit
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
import kotlinx.coroutines.launch
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

suspend fun CoreModule.settingsCommand() = publicSlashCommand {
    name = "settings"
    description = "Einstellungen"
    check {
        hasPermission(Permission.ManageGuild)
    }
    channelSettingsSubCommand()
    /*action {
        if (arguments.channelBarrierChannel != null || arguments.channelBarrierName != null) {
            if (arguments.channelBarrierName == null || arguments.channelBarrierChannel == null) {
                respondEphemeral {
                    content = translate("command.settings.channel_barrier.arguments")
                }
                return@action
            }
            val channel = arguments.channelBarrierChannel as Channel
            val name = (arguments.channelBarrierName as String).lowercase()
            newSuspendedTransaction {
                ChannelBarrier.new {
                    this.name = name
                    channelId = channel.toLong()
                    guildId = guild!!.toLong()
                }
                respond {
                    content = translate("command.settings.success")
                }
            }
            return@action
        }
        if (arguments.extra != null) {
            when (arguments.extra!!) {
                "save_config" -> {
                    this@settingsCommand.config.save()
                    respondEphemeral {
                        content = "Config wurde gespeichert!"
                    }
                }
                else -> {
                    respondEphemeral {
                        content = "Invalides Extra!"
                    }
                }
            }
            return@action
        }
    }*/
}

context(CoreModule)
        private suspend fun PublicSlashCommand<*>.channelSettingsSubCommand() =
    ephemeralSubCommand(::ChannelSettingsArguments) {
        name = "channel"
        description = "Channel Einstellungen"
        check {
            hasPermission(Permission.ManageGuild)
        }
        action {
            val channel = guild!!.getChannel(arguments.channel.id)
            val message = respond {
                embed {
                    title = "Channel Einstellungen/Features für ${channel.data.name.value ?: "?"}"
                    description = "Loading..."
                }
            }
            this@ephemeralSubCommand.kord.launch {
                val channelFeature = ChannelFeature.feature(channel)
                message.edit {
                    embed {
                        title = "Channel Einstellungen/Features für ${channel.data.name.value ?: "?"}"
                        selfAuthor()
                        description = channel.mention
                        for ((feature, active) in channelFeature.features) {
                            val emoji = (if (active) ":green" else ":red") + "_square:"
                            field("$emoji ${feature.name}")
                        }
                    }
                    components(2.minutes) {
                        var selectedFeature: ChannelFeatureType<*>? = null
                        publicButton {
                            deferredAck = true
                            label = "Aktivieren"
                            style = ButtonStyle.Success
                            id = ENABLE_BUTTON
                            action inner@{
                                channelFeature.changeState(selectedFeature, true)
                            }
                        }
                        publicButton {
                            label = "Deaktivieren"
                            style = ButtonStyle.Danger
                            id = DISABLE_BUTTON
                            action {
                                channelFeature.changeState(selectedFeature, false)
                            }
                        }

                        publicSelectMenu {
                            option("ChannelBarrier", "channel_barrier") {
                                description =
                                    "Aktionen, die nur in einem Channel (einer Kategorie) ausgeführt werden sollen"
                                option("OneMessage", "one_message") {
                                    description = "Es kann nur eine Nachricht pro User gesendet werden"
                                }
                                minimumChoices = 1
                                maximumChoices = 1
                                action {
                                    /*val active = channelFeature
                                    if (active) {
                                        enableButton.disable()
                                        disableButton.enable()
                                    } else {
                                        enableButton.enable()
                                        disableButton.disabled
                                    }*/
                                    selectedFeature = ChannelFeatureType.fromStringOrElseThrow(selected.first())
                                }
                            }
                        }
                    }
                }
            }
        }
    }

context(PublicInteractionButtonContext)
        suspend fun ChannelFeature.changeState(feature: ChannelFeatureType<*>?, to: Boolean) {
    if (feature == null) {
        respondEphemeral {
            content = "bitte wähle eine Option!"
        }
        return
    }
    features[feature] = to
    update()
}
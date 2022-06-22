/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.core.commands

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalChannel
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.types.respondEphemeral
import de.jvstvshd.chillingfoxes.foxesbot.io.ChannelBarrier
import de.jvstvshd.chillingfoxes.foxesbot.module.core.CoreModule
import de.jvstvshd.chillingfoxes.foxesbot.util.KordUtil.toLong
import dev.kord.common.entity.Permission
import dev.kord.core.entity.channel.Channel
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class SettingsArguments : Arguments() {
    val channelBarrierName by optionalString {
        name = "channel_barrier_name"
        description = "Name für eine Barriere für z.B. Nachrichten oder Commands für bestimmte Channel"
    }
    val channelBarrierChannel by optionalChannel {
        name = "channel_barrier_channel"
        description = "Channel für die Channel-Barriere"
    }
    val extra by optionalString {
        name = "extra"
        description = "Extra"
    }
}

suspend fun CoreModule.settingsCommand() = publicSlashCommand(::SettingsArguments) {
    name = "settings"
    description = "Einstellungen"
    check {
        hasPermission(Permission.ManageGuild)
    }
    action {
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
    }
}
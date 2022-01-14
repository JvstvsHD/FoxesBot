package de.jvstvshd.chillingfoxes.foxesbot.module.core.commands

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalChannel
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalLong
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.types.respondEphemeral
import de.jvstvshd.chillingfoxes.foxesbot.module.core.CoreModule
import de.jvstvshd.chillingfoxes.foxesbot.util.KordUtil.toLong
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Permission
import dev.kord.core.entity.channel.Channel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SettingsArguments : Arguments() {
    val channelBarrierName by optionalString(
        "channel_barrier_name",
        "Name für eine Barriere für z.B. Nachrichten oder Commands für bestimmte Channel"
    )
    val channelBarrierChannel by optionalChannel("channel_barrier_channel", "Channel für die Channel-Barriere")
    val extra by optionalString("extra", "Extra")
    val throwCooldown by optionalLong("cooldown", "Cooldown zwischen Würfen")
}

@OptIn(DelicateCoroutinesApi::class)
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
            println(channel.type)
            if (channel.type != ChannelType.GuildText) {
                respondEphemeral {
                    content = translate("command.settings.channel_barrier.channel")
                }
                return@action
            }
            GlobalScope.launch {
                this@settingsCommand.dataSource.connection.use { connection ->
                    connection.prepareStatement("INSERT INTO channel_barriers (name, channel_id, guild_id) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE channel_id = ?, guild_id = ?")
                        .use {
                            try {
                                it.setString(1, name)
                                it.setLong(2, channel.toLong())
                                it.setLong(3, guild!!.toLong())
                                it.setLong(4, channel.toLong())
                                it.setLong(5, guild!!.toLong())
                                it.executeUpdate()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
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
                        ephemeral = true
                        content = "Config wurde gespeichert!"
                    }
                }
                else -> {
                    respondEphemeral {
                        ephemeral = true
                        content = "Invalides Extra!"
                    }
                }
            }
            return@action
        }
        if (arguments.throwCooldown != null) {
            config.configData.eventData.throwCooldown = arguments.throwCooldown!!
            config.save()
            respond {
                content = "Die Einstellungen wurden erfolgreich aktualisiert."
            }
        }
    }
}
package de.jvstvshd.foxesbot.module.core.commands

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalChannel
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalString
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.types.respondEphemeral
import de.jvstvshd.foxesbot.module.core.CoreModule
import de.jvstvshd.foxesbot.utils.KordUtil.toLong
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Permission
import dev.kord.core.entity.channel.Channel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class SettingsArguments : Arguments() {
    val channelBarrierName by optionalString(
        "channel_barrier_name",
        "Name für eine Barriere für z.B. Nachrichten oder Commands für bestimmte Channel"
    )
    val channelBarrierChannel by optionalChannel("channel_barrier_channel", "Channel für die Channel-Barriere")
}

@OptIn(DelicateCoroutinesApi::class)
suspend fun CoreModule.settingsCommand() = publicSlashCommand(::SettingsArguments) {
    name = "settings"
    description = "Einstellungen"
    requireBotPermissions(Permission.ManageGuild)
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
            GlobalScope.async {
                this@settingsCommand.dataSource.connection.use { connection ->
                    connection.prepareStatement("INSERT INTO channel_barriers (name, channel_id) VALUES (?, ?) ON DUPLICATE KEY UPDATE channel_id = ?")
                        .use {
                            it.setString(1, name)
                            it.setLong(2, channel.toLong())
                            it.setLong(3, channel.toLong())
                            it.executeUpdate()
                        }
                }
                respond {
                    content = translate("command.settings.success")
                }
            }
        }
    }
}
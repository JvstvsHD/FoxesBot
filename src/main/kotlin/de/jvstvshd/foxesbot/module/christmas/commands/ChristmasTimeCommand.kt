package de.jvstvshd.foxesbot.module.christmas.commands

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.application.slash.publicSubCommand
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.jvstvshd.foxesbot.module.christmas.ChristmasModule
import dev.kord.common.annotation.KordVoice
import dev.kord.common.entity.Permission
import dev.kord.core.entity.channel.StageChannel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

@OptIn(KordVoice::class)
suspend fun ChristmasModule.christmasTimeCommand() = publicSlashCommand {
    name = "weihnachtstime"
    description = "Joint in die Weihnachtstime"
    publicSubCommand {
        name = "exit"
        description = "Beendet die Weihnachtstime"
        check {
            hasPermission(Permission.ManageChannels)
        }
        action {
            val player = christmasTimes[guild!!.id]
            if (player == null) {
                respond {
                    content = "Es wird derzeit keine Musik gespielt"
                }
                return@action

            }
            player.exit()
            respond {
                content = "Die Verbindung wurde erfolgreich getrennt."
            }
            return@action
        }
    }
    publicSubCommand {
        name = "play"
        description = "Betritt die Weihnachtstime"
        check {
            hasPermission(Permission.ManageChannels)
        }
        action {
            val channel = guild!!.channels.filter { it is StageChannel }.first()
                .asChannel() as StageChannel
            christmasTime(channel)
            respond {
                content = "Ab jetzt genießt ihr bis 20:00 Uhr Weihnachtsmusik!"
            }
            return@action
        }
    }
}
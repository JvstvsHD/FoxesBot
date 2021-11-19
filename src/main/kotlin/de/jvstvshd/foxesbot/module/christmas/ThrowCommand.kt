@file:Suppress("DeferredResultUnused")

package de.jvstvshd.foxesbot.module.christmas

import com.kotlindiscord.kord.extensions.DISCORD_GREEN
import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.DISCORD_YELLOW
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalInt
import com.kotlindiscord.kord.extensions.extensions.chatCommand
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.runSuspended
import de.jvstvshd.foxesbot.utils.KordUtil
import de.jvstvshd.foxesbot.utils.KordUtil.toLong
import dev.kord.common.Color
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.reply
import dev.kord.core.entity.channel.TextChannel
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class ThrowArguments : Arguments() {
    val amount by optionalInt("amount", "Anzahl an Schneebällen, die geworfen werden soll.")
}

@OptIn(DelicateCoroutinesApi::class)
suspend fun ChristmasModule.throwCommand() = ephemeralSlashCommand(::ThrowArguments) {
    name = "werfen"
    description = "Werfe deine Schneebälle"
    action {
        GlobalScope.async(CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
        }) {
            val executionChannel = this@throwCommand.getChannel()
            println(executionChannel?.name)
            if (executionChannel == null || executionChannel.toLong() != channel.toLong()) {
                respond {
                    content = "Bitte führe den Command in " + if (executionChannel == null) "undefiniert" else executionChannel.mention + " aus"
                }
                return@async
            }
            val snowballs = getSnowballs(member!!.toLong())
            val acquired = arguments.amount ?: 1
            if (snowballs <= 0 || acquired > snowballs) {
                respond {
                    embed {
                        description = "Du hast nicht genügend Schneebälle: $snowballs ($acquired > $snowballs)"
                        color = DISCORD_RED
                    }
                    //ephemeral = true
                }
                return@async
            }
            dataSource.connection.use { connection ->
                connection.prepareStatement("INSERT INTO snowballs (id, snowballs) VALUES (?, ?) ON DUPLICATE KEY UPDATE snowballs = snowballs - ?;")
                    .use {
                        it.setLong(1, member!!.toLong())
                        it.setLong(2, (6 - acquired).toLong())
                        it.setInt(3, acquired)
                        it.executeUpdate()
                    }
            }
            val newHp = changeSnowMonster(guild!!.toLong(), acquired)
            val oldHp = if (newHp < 0) changeSnowMonster(guild!!.toLong(), -(0 - newHp)) else newHp + acquired
            val actualHp = if (newHp < 0) 0 else newHp
            println(oldHp)
            executionChannel.createEmbed {
                description = "Das Schneemonster hat nun nur noch $actualHp/1000 (${(actualHp / 1000.0) * 100}%) HP"
                color = getColor(newHp)
            }
            respond {
                content =
                    "Du hast dem Schneemonster ${if (newHp - actualHp != acquired) oldHp - actualHp else acquired} HP abgezogen!"
            }
            if (oldHp - (oldHp % 100.0) != actualHp - (actualHp % 100.0) && actualHp <= 900) {
                val result = callEvent(executionChannel.asChannel(), actualHp)
                if (result > 0) {
                    changeSnowMonster(guild!!.toLong(), actualHp - result)
                }
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
suspend fun ChristmasModule.throwChatCommand() = chatCommand(::ThrowArguments) {
    name = "werfen"
    description = "Werfe deine Schneebälle"
    action {
        GlobalScope.async(CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
        }) {
            val executionChannel = channel.asChannelOrNull() as TextChannel?
            println(executionChannel?.name)
            if (executionChannel == null || executionChannel.toLong() != channel.toLong()) {
                message.reply {
                    content = "Bitte führe den Command in " + if (executionChannel == null) "undefiniert" else executionChannel.mention + " aus"
                }
                return@async
            }
            val snowballs = getSnowballs(member!!.toLong())
            val acquired = arguments.amount ?: 1
            if (snowballs <= 0 || acquired > snowballs) {
                message.reply {
                    embed {
                        description = "Du hast nicht genügend Schneebälle: $snowballs ($acquired > $snowballs)"
                        color = DISCORD_RED
                    }
                }
                return@async
            }
            dataSource.connection.use { connection ->
                connection.prepareStatement("INSERT INTO snowballs (id, snowballs) VALUES (?, ?) ON DUPLICATE KEY UPDATE snowballs = snowballs - ?;")
                    .use {
                        it.setLong(1, member!!.toLong())
                        it.setLong(2, (6 - acquired).toLong())
                        it.setInt(3, acquired)
                        it.executeUpdate()
                    }
            }
            val newHp = changeSnowMonster(guild!!.toLong(), acquired)
            val oldHp = if (newHp < 0) changeSnowMonster(guild!!.toLong(), -(0 - newHp)) else newHp + acquired
            val actualHp = if (newHp < 0) 0 else newHp
            println(oldHp)
            executionChannel.createEmbed {
                description = "Das Schneemonster hat nun nur noch $actualHp/1000 (${(actualHp / 1000.0) * 100}%) HP"
                color = getColor(newHp)
            }
            message.reply {
                content =
                    "Du hast dem Schneemonster ${if (newHp - actualHp != acquired) oldHp - actualHp else acquired} HP abgezogen!"
            }
            if (oldHp - (oldHp % 100.0) != actualHp - (actualHp % 100.0) && actualHp <= 900) {
                val result = callEvent(executionChannel.asChannel(), actualHp)
                if (result > 0) {
                    changeSnowMonster(guild!!.toLong(), actualHp - result)
                }
            }
        }
    }
}

private suspend fun callEvent(channel: TextChannel, startHp: Int): Int {
    val event = HpEvent.pickRandom()
    val result = event.execute(startHp)

    channel.createEmbed {
        event.sendMessage(this)
        title = "event"
    }
    if (result.type == Type.HP) {
        return result.newHp
    }
    return 0
}

private fun getColor(hp: Int): Color {
    return when {
        hp >= 750 -> KordUtil.convertColor(java.awt.Color.RED)
        hp >= 500 -> DISCORD_RED
        hp >= 250 -> DISCORD_YELLOW
        hp >= 100 -> Color(0, 100, 0)
        else -> DISCORD_GREEN
    }
}

private suspend fun ChristmasModule.getSnowballs(long: Long) = runSuspended {
    dataSource.connection.use { connection ->
        connection.prepareStatement("SELECT snowballs FROM snowballs WHERE id = ?;").use {
            it.setLong(1, long)
            val rs = it.executeQuery()
            if (rs.next()) {
                return@runSuspended rs.getLong(1)
            } else {
                -1
            }
        }
    }

}
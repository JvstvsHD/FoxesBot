@file:Suppress("DeferredResultUnused")

package de.jvstvshd.foxesbot.module.christmas.commands

import com.kotlindiscord.kord.extensions.DISCORD_GREEN
import com.kotlindiscord.kord.extensions.DISCORD_RED
import com.kotlindiscord.kord.extensions.DISCORD_YELLOW
import com.kotlindiscord.kord.extensions.extensions.chatCommand
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.respond
import com.kotlindiscord.kord.extensions.utils.runSuspended
import de.jvstvshd.foxesbot.config.Config
import de.jvstvshd.foxesbot.module.christmas.*
import de.jvstvshd.foxesbot.util.KordUtil
import de.jvstvshd.foxesbot.util.KordUtil.toLong
import dev.kord.common.Color
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.reply
import dev.kord.core.entity.Member
import dev.kord.core.entity.channel.TextChannel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ThreadLocalRandom

@OptIn(DelicateCoroutinesApi::class)
suspend fun ChristmasModule.throwCommand() = ephemeralSlashCommand {
    name = "werfen"
    description = "Werfe deine Schneebälle"
    action {
        GlobalScope.launch(CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
        }) {
            if (onCooldown(member!!.toLong())) {
                respond {
                    content = "Bitte verwende diesen Command nur alle 24 Minuten!"
                }
                return@launch
            }
            val executionChannel = this@throwCommand.getChannel(guild!!.toLong())
            if (executionChannel == null || executionChannel.toLong() != channel.toLong()) {
                respond {
                    content =
                        "Bitte führe den Command in " + if (executionChannel == null) "undefiniert" else executionChannel.mention + " aus"
                }
                return@launch
            }
            val snowballs = getSnowballs(member!!.toLong())
            if (snowballs <= 0) {
                respond {
                    content = "Der Schnee ist bedauerlicherweise bereite getaut. Warte, bis es erneut schneit!"
                }
                return@launch
            }
            val throwing = ThreadLocalRandom.current().nextLong(1, snowballs + 1)
            respond {
                content =
                    throwSnowballs(throwing.toInt(), member!!.asMember(), channel.asChannel() as TextChannel, snowballs)
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
suspend fun ChristmasModule.throwChatCommand() = chatCommand {
    name = "werfen"
    description = "Werfe deine Schneebälle"
    action {
        GlobalScope.launch(CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
        }) {
            if (onCooldown(member!!.toLong())) {
                val cooldown = config.configData.eventData.throwCooldown
                val formatted =
                    if (cooldown < 60.0) ("$cooldown Sekunden") else "${DecimalFormat("#.##").format(cooldown / 60.0)} Minuten"
                message.reply {
                    content = "Bitte verwende diesen Command nur alle $formatted!"
                }
                return@launch
            }
            val executionChannel = this@throwChatCommand.getChannel(guild!!.toLong())
            if (executionChannel == null || executionChannel.toLong() != channel.toLong()) {
                message.reply {
                    content =
                        "Bitte führe den Command in " + if (executionChannel == null) "undefiniert" else executionChannel.mention + " aus"
                }
                return@launch
            }
            val snowballs = getSnowballs(member!!.toLong())
            if (snowballs <= 0) {
                message.reply {
                    content = "Der Schnee ist bedauerlicherweise bereite getaut. Warte, bis es erneut schneit!"
                }
                return@launch
            }
            val throwing = ThreadLocalRandom.current().nextLong(1, snowballs + 1)
            message.respond(
                throwSnowballs(
                    throwing.toInt(),
                    member!!.asMember(),
                    channel.asChannel() as TextChannel,
                    snowballs
                )
            )
        }
    }
}

private suspend fun ChristmasModule.onCooldown(memberId: Long): Boolean {
    val lastTime = lastTime(memberId, "command") ?: return false
    return Duration.between(lastTime, LocalDateTime.now()).toSeconds() <= config.configData.eventData.throwCooldown
}

private suspend fun ChristmasModule.documentUse(memberId: Long) = document(memberId, "command")

private suspend fun ChristmasModule.throwSnowballs(
    amount: Int,
    member: Member,
    channel: TextChannel,
    memberHp: Long
): String {
    dataSource.connection.use { connection ->
        connection.prepareStatement("INSERT INTO snowballs (id, snowballs) VALUES (?, ?) ON DUPLICATE KEY UPDATE snowballs = snowballs - ?;")
            .use {
                it.setLong(1, member.toLong())
                it.setLong(2, (6 - amount).toLong())
                it.setInt(3, amount)
                it.executeUpdate()
            }
    }
    val oldHp = getSnowMonsterHp(member.guild.toLong())
    val newHp = calculateNewHp(amount, oldHp)
    return performThrow(
        oldHp,
        newHp,
        amount,
        member,
        channel,
        memberHp
    )
}

private suspend fun ChristmasModule.performThrow(
    oldHp: Int,
    newHp: Int,
    amount: Int,
    member: Member,
    channel: TextChannel,
    memberHp: Long
): String {
    if (oldHp != newHp) {
        changeSnowMonster(member.guild.toLong(), newHp)
        documentUse(member.toLong())
    }
    channel.createEmbed {
        description =
            "Das Schneemonster hat nun nur noch $newHp/1000 (${DecimalFormat("#.#").format((newHp / 1000.0) * 100)}%) HP"
        color = getColor(newHp)
    }
    val extraSnowball: Boolean
    val finalAmount: Int
    if (ThreadLocalRandom.current().nextDouble() <= 0.05) {
        extraSnowball = true
        finalAmount = amount + 1
    } else {
        extraSnowball = false
        finalAmount = amount
    }
    if (oldHp - (oldHp % 100.0) != newHp - (newHp % 100.0) && newHp <= 900) {
        val result = callEvent(channel.asChannel(), newHp, config, member, memberHp)
        if (result > 0) {
            changeSnowMonster(member.getGuild().toLong(), newHp - result)
        }
    }
    val thrownAmount = if (newHp - newHp != amount) oldHp - newHp else amount
    if (extraSnowball) {
        return "Oho! Wo kommt denn der $finalAmount. Schneeball her?"
    }
    return when (thrownAmount) {
        1 -> "Ein gezielter Wurf, ein Treffer, 1 Leben weniger!"
        2 -> "D d d Doppel Shot, das Schneemonster hat nun 2 HP weniger!"
        3 -> "Alle gute Dinge sind? Drei, genau, das Schneemonster hat 3 HP verloren!"
        4 -> "Du hast dem Schneemonster 4 HP abgezogen!"
        5 -> "Oha, das war aber ein harter Treffer, 5 HP auf einmal abgezogen!"
        6 -> "WOW! Du hast alle Deine 6 Schneebälle auf einmal geworfen!"
        in 7..Int.MAX_VALUE -> "Alles klar, $finalAmount Schneebälle?"
        else -> "Wie konntest du denn $finalAmount Schneebälle werfen? Erzähle das doch unserem lieben Support einmal!"
    }
}

private fun calculateNewHp(acquired: Int, oldHp: Int): Int = if (oldHp - acquired < 0) 0 else oldHp - acquired


private suspend fun ChristmasModule.callEvent(
    channel: TextChannel,
    startHp: Int,
    config: Config,
    member: Member,
    memberHp: Long
): Int {
    val event = HpEvent.pickRandom()
    val result = event.execute(startHp)

    channel.createEmbed {
        event.sendMessage(this)
        title = "event"
    }
    if (event is LimitExpansion) {
        config.configData.eventData.snowballLimit += 1
        config.save()
    } else if (event is SnowballGain) {
        var extra = ThreadLocalRandom.current().nextLong(1, 4)
        if (memberHp + extra > config.configData.eventData.snowballLimit) {
            extra = config.configData.eventData.snowballLimit - memberHp
        }
        dataSource.connection.use { connection ->
            connection.prepareStatement("UPDATE snowballs SET snowballs = ? WHERE id = ?;").use {
                it.setLong(2, (extra + memberHp).toLong())
                it.setLong(1, member.toLong())
                it.executeUpdate()
            }
        }
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
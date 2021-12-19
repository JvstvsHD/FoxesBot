package de.jvstvshd.foxesbot.module.christmas.commands

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.member
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalLong
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.jvstvshd.foxesbot.module.christmas.ChristmasModule
import de.jvstvshd.foxesbot.util.KordUtil.toLong
import de.jvstvshd.foxesbot.util.KordUtil.toSnowflake
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.channel.editRolePermission
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.supplier.EntitySupplyStrategy
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

open class AdministrationArgs : Arguments() {
    val set by optionalLong("set", "Absoluter Wert")
    val add by optionalLong("add", "Relativer Wert")
}

class SnowballAdministrationArgs : Arguments() {
    val user by member("user", "Schneebälle des Users")
    val set by optionalLong("set", "Absoluter Wert")
    val add by optionalLong("add", "Relativer Wert")
}

class SnowMonsterAdministrationArgs : AdministrationArgs()

suspend fun ChristmasModule.administrationCommands() {
    snowballAdministrationCommand()
    snowMonsterAdministrationCommand()
}

@OptIn(DelicateCoroutinesApi::class)
suspend fun ChristmasModule.snowballAdministrationCommand() = ephemeralSlashCommand(::SnowballAdministrationArgs) {
    name = "snowballs"
    description = "Schneeball-Verwaltung"
    check {
        hasPermission(Permission.ManageGuild)
    }
    action {
        GlobalScope.launch {
            val member = arguments.user
            val query: String
            val amount: Long
            if (arguments.add != null) {
                query =
                    "INSERT INTO snowballs (id, snowballs) VALUES (?, ?) ON DUPLICATE KEY UPDATE snowballs = snowballs + ?;"
                amount = arguments.add!!
            } else if (arguments.set != null) {
                query = "INSERT INTO snowballs (id, snowballs) VALUES (?, ?) ON DUPLICATE KEY UPDATE snowballs = ?;"
                amount = arguments.set!!
            } else {
                dataSource.connection.use { connection ->
                    connection.prepareStatement("SELECT snowballs FROM snowballs WHERE id = ?;").use {
                        it.setLong(1, member.toLong())
                        val rs = it.executeQuery()
                        respond {
                            content =
                                "${member.mention} hat derzeit ${if (rs.next()) rs.getLong(1) else -1} Schneebälle."
                        }
                    }
                }
                return@launch
            }
            dataSource.connection.use { connection ->
                connection.prepareStatement(query)
                    .use {
                        it.setLong(1, member.toLong())
                        it.setLong(2, amount)
                        it.setLong(3, amount)
                        respond {
                            content = "Es wurden ${it.executeUpdate()} Reihen in der Datenbank geändert."
                        }
                        return@launch
                    }
            }
        }

    }
}

@OptIn(DelicateCoroutinesApi::class)
suspend fun ChristmasModule.snowMonsterAdministrationCommand() =
    ephemeralSlashCommand(::SnowMonsterAdministrationArgs) {
        name = "snowmonster"
        description = "Verwaltet das Schneemonster"
        check {
            hasPermission(Permission.ManageGuild)
        }
        action {
            GlobalScope.launch {
                val query: String
                val amount: Long
                if (arguments.set != null) {
                    query = "INSERT INTO snow_monster (guild_id, hp) VALUES (?, ?) ON DUPLICATE KEY UPDATE hp = ?;"
                    amount = arguments.set!!
                } else if (arguments.add != null) {
                    query = "INSERT INTO snow_monster (guild_id, hp) VALUES (?, ?) ON DUPLICATE KEY UPDATE hp = hp + ?;"
                    amount = arguments.add!!
                } else {
                    dataSource.connection.use { connection ->
                        connection.prepareStatement("SELECT hp FROM snow_monster WHERE guild_id = ?;").use {
                            it.setLong(1, guild!!.toLong())
                            val rs = it.executeQuery()
                            respond {
                                content =
                                    "Die Anzahl der Leben dieses Schneemonsters betragen ${if (rs.next()) rs.getLong(1) else -1}."
                            }
                            return@launch
                        }
                    }
                }
                dataSource.connection.use { connection ->
                    connection.prepareStatement(query).use {
                        it.setLong(1, guild!!.toLong())
                        it.setInt(2, amount.toInt())
                        it.setInt(3, amount.toInt())
                        respond {
                            content = "Es wurden ${it.executeUpdate()} Reihen in der Datenbank geändert."
                        }
                        return@launch
                    }
                }
            }
        }
    }

suspend fun ChristmasModule.winCommand() = ephemeralSlashCommand {
    name = "win"
    description = "WIN!"
    action {
        var winners = ""
        dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT id FROM christmas_stats WHERE type = ? ORDER BY count DESC LIMIT ?;")
                .use {
                    it.setString(1, "thrown_snowballs")
                    it.setInt(2, 3)
                    val rs = it.executeQuery()
                    var count = 1
                    while (rs.next()) {
                        winners += this@winCommand.kord.getUser(
                            rs.getLong(1).toSnowflake(),
                            EntitySupplyStrategy.cacheWithCachingRestFallback
                        )?.mention ?: "Unbekannter User :c"
                        if (count < 2) {
                            winners += ", "
                        } else if (count == 2) {
                            winners += " und "
                        }
                        count++
                    }
                }
        }
        val guild = member!!.guild
        val fightChannel = guild.getChannel(907682138277703690.toSnowflake()) as TextChannel
        fightChannel.editRolePermission(720189868059394118.toSnowflake()) {
            denied = Permissions(Permission.SendMessages)
        }
        val hutChannel = guild.getChannel(921757760121630751.toSnowflake()) as TextChannel
        hutChannel.editRolePermission(720189868059394118.toSnowflake()) {
            allowed = Permissions(Permission.ViewChannel)
        }
        fightChannel.createMessage {
            content = "Glückwunsch, ihr habt das Schneemonster besiegt!\n" +
                    "Ein großer Dank geht an $winners und natürlich an alle Anderen, die mitgeholfen haben!\n" +
                    "Jetzt ist der Weg zur Hütte frei...\n"
        }
        fightChannel.createMessage("https://cdn.discordapp.com/attachments/654335565369442304/921867947515994192/Snowman.png")
        hutChannel.createMessage {
            content = "https://cdn.discordapp.com/attachments/654335565369442304/921867947515994192/Snowman.png"
        }
        //https://cdn.discordapp.com/attachments/654335565369442304/921867947515994192/Snowman.png
    }
}
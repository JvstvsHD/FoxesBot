package de.jvstvshd.foxesbot.module.christmas.commands

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.impl.member
import com.kotlindiscord.kord.extensions.commands.converters.impl.optionalLong
import com.kotlindiscord.kord.extensions.extensions.ephemeralSlashCommand
import com.kotlindiscord.kord.extensions.types.respond
import de.jvstvshd.foxesbot.module.christmas.ChristmasModule
import de.jvstvshd.foxesbot.util.KordUtil.toLong
import dev.kord.common.entity.Permission
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


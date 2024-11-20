/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.core.commands

import de.jvstvshd.chillingfoxes.foxesbot.module.core.CoreModule
import dev.kord.common.entity.Permission
import dev.kordex.core.checks.hasPermission
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.i18n.toKey

suspend fun CoreModule.testCommand() = publicSlashCommand {
    name = "test".toKey()
    description = "TEST 1 2 3".toKey()
    check {
        hasPermission(Permission.ManageGuild)
    }
    action {
        throw IllegalStateException("command is illegal")
    }
}
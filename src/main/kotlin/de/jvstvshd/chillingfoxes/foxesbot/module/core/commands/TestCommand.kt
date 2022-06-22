/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.core.commands

import com.kotlindiscord.kord.extensions.checks.hasPermission
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import de.jvstvshd.chillingfoxes.foxesbot.module.core.CoreModule
import dev.kord.common.entity.Permission

suspend fun CoreModule.testCommand() = publicSlashCommand {
    name = "test"
    description = "TEST 1 2 3"
    check {
        hasPermission(Permission.ManageGuild)
    }
    action {
        throw IllegalStateException("command is illegal")
    }
}
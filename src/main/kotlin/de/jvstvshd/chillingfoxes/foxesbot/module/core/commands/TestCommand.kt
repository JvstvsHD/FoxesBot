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
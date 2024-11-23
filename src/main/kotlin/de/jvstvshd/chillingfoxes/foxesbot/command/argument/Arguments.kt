/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.command.argument

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.member
import dev.kordex.core.commands.converters.impl.optionalMember
import dev.kordex.core.i18n.toKey

open class MemberArguments : Arguments() {
    val member by member {
        name = "member".toKey()
        description = "Mitglied des Servers".toKey()
    }
}

open class OptionalMemberArguments : Arguments() {
    val member by optionalMember {
        name = "member".toKey()
        description = "Mitglied des Servers".toKey()
    }
}
/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.member

import de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.EntityFeatureData
import de.jvstvshd.chillingfoxes.foxesbot.module.core.settings.EntityFeatureType
import dev.kord.core.behavior.MemberBehavior

sealed class MemberFeatureType<T : EntityFeatureData<MemberBehavior>>(override val id: String) :
    EntityFeatureType<T, MemberBehavior> {

    object SuppressPresenceCheck : MemberFeatureType<EntityFeatureData<MemberBehavior>>("suppress_presence_check") {
        override val name: String
            get() = "Präsenz-Check: Unterdrückung"
        override val description: String
            get() = "Unterdrückt Abfragen des Präsenz-Status"
    }

    companion object {

        val features: List<MemberFeatureType<EntityFeatureData<MemberBehavior>>> = listOf()

        fun fromStringOrElseThrow(type: String): MemberFeatureType<out EntityFeatureData<MemberBehavior>> =
            fromString(type) ?: error("unknown channel feature type $type")

        fun fromString(name: String): MemberFeatureType<out EntityFeatureData<MemberBehavior>>? =
            features.find { it.id == name.lowercase() }

    }

    object BotAdmin : MemberFeatureType<EntityFeatureData<MemberBehavior>>("bot_admin") {

        override val name: String
            get() = "BotAdmin"
        override val description: String
            get() = "Gewährt Zugriff auf alle (sobald ich es ändere) Funktionen des Bots"
    }
}
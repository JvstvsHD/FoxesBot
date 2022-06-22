/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.core.music

enum class MusicState(val readableName: String) {
    ACTIVATED("aktiviert"),
    DEACTIVATED("deaktiviert"),
    DELETED("gelöscht (wtf?!)"),
    UNKNOWN("unbekannt (WARUM???)");

    val playable by lazy { this == ACTIVATED }
}
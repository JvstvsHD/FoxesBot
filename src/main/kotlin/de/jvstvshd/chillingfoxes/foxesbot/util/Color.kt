/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

@file:Suppress("unused")

package de.jvstvshd.chillingfoxes.foxesbot.util

import dev.kord.common.Color
import dev.kord.rest.builder.message.EmbedBuilder

fun EmbedBuilder.color(awtColor: java.awt.Color) {
    this.color = Color(awtColor.rgb)
}

/**
 * Same values as [adventure's NamedTextColor](https://github.com/KyoriPowered/adventure/blob/main/4/api/src/main/java/net/kyori/adventure/text/format/NamedTextColor.java)
 */
object Colors {

    val BLACK = Color(0x000000)
    val DARK_BLUE = Color(0x0000aa)
    val DARK_GREEN = Color(0x00aa00)
    val DARK_AQUA = Color(0x00aaaa)
    val DARK_RED = Color(0xaa0000)
    val DARK_PURPLE = Color(0xaa00aa)
    val GOLD = Color(0xffaa00)
    val GRAY = Color(0xaaaaaa)
    val DARK_GRAY = Color(0x555555)
    val BLUE = Color(0x5555ff)
    val GREEN = Color(0x55ff55)
    val AQUA = Color(0x55ffff)
    val RED = Color(0xff5555)
    val LIGHT_PURPLE = Color(0xff55ff)
    val YELLOW = Color(0xffff55)
    val WHITE = Color(0xffffff)
}
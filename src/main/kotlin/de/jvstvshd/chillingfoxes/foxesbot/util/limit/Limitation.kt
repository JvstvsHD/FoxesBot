/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.util.limit

interface Limitation {

    fun limitExceeded(): Boolean

    fun limitNow()

    fun access()

    fun limit(): String

    fun shouldLimit(): Boolean
}

class UnlimitedLimitation : Limitation {

    override fun limitExceeded(): Boolean = false

    override fun limitNow() {}

    override fun access() {}

    override fun limit(): String = "unlimited"

    override fun shouldLimit(): Boolean = false
}
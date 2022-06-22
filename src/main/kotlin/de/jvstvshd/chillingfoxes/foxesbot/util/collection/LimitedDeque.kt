/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.util.collection

import de.jvstvshd.chillingfoxes.foxesbot.util.limit.LimitExceededException
import de.jvstvshd.chillingfoxes.foxesbot.util.limit.Limitation
import java.util.*

class LimitedDeque<E>(val limitation: Limitation) : LimitedQueue<E>, ArrayDeque<E>() {

    override fun limited(): Boolean =
        limitation.limitExceeded()

    private fun checkLimitation() {
        if (limited())
            throw LimitExceededException("limit ${limitation.limit()} was exceeded.")
    }

    private fun checkAccess() {
        checkLimitation()
        limitation.access()
    }

    override fun pollFirst(): E {
        checkAccess()
        return super.pollFirst()
    }

    override fun pollLast(): E {
        checkAccess()
        return super.pollLast()
    }

    fun pollUnchecked(): E = poll()
}
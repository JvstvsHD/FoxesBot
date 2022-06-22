/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.util.limit

abstract class NumberBasedLimitation<T : Number>(open val maxCount: T, private val startValue: T) : Limitation {

    var count: T = startValue

    override fun limitNow() {
        count = maxCount
    }

    override fun toString(): String {
        return "NumberBasedLimitation(maxCount=$maxCount, startValue=$startValue, count=$count)"
    }
}

class LongBasedLimitation(@Suppress("MemberVisibilityCanBePrivate") override val maxCount: Long) :
    NumberBasedLimitation<Long>(maxCount, 0L) {

    init {
        if (maxCount < 0) {
            throw IllegalArgumentException("max count may not be less than zero")
        }
    }

    override fun limitExceeded(): Boolean = count > maxCount

    override fun access() {
        count++
    }

    override fun limit(): String = maxCount.toString()

    override fun shouldLimit(): Boolean = count == maxCount
}

class IntBasedLimitation(@Suppress("MemberVisibilityCanBePrivate") override val maxCount: Int) :
    NumberBasedLimitation<Int>(maxCount, 0) {

    init {
        if (maxCount < 0) {
            throw IllegalArgumentException("max count may not be less than zero")
        }
    }

    override fun limitExceeded(): Boolean = count > maxCount

    override fun access() {
        count++
    }

    override fun limit(): String = maxCount.toString()

    override fun shouldLimit(): Boolean = count == maxCount
}
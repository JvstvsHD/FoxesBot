package de.jvstvshd.foxesbot.util.limit

class CountBasedLimitation(@Suppress("MemberVisibilityCanBePrivate") val maxCount: Long) : Limitation {

    init {
        if (maxCount < 0) {
            throw IllegalArgumentException("max count may not be less than zero")
        }
    }

    var count: Long = 0

    override fun limitExceeded(): Boolean = count > maxCount

    override fun limitNow() {
        count = maxCount
    }

    override fun access() {
        count++
    }

    override fun limit(): String = maxCount.toString()

    override fun shouldLimit(): Boolean = count == maxCount
}
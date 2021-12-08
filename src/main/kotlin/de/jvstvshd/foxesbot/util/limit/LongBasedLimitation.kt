package de.jvstvshd.foxesbot.util.limit

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
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
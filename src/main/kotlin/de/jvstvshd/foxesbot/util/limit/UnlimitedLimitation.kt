package de.jvstvshd.foxesbot.util.limit

class UnlimitedLimitation : Limitation {

    override fun limitExceeded(): Boolean = false

    override fun limitNow() {}

    override fun access() {}

    override fun limit(): String = "unlimited"

    override fun shouldLimit(): Boolean = false
}
package de.jvstvshd.foxesbot.util.limit

interface Limitation {

    fun limitExceeded(): Boolean

    fun limitNow()

    fun access()

    fun limit(): String

    fun shouldLimit(): Boolean

}
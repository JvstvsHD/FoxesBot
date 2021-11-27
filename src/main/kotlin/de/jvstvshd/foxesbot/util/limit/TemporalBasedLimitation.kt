package de.jvstvshd.foxesbot.util.limit

import java.time.temporal.TemporalAccessor

abstract class TemporalBasedLimitation<T : TemporalAccessor>(open val end: T) : Limitation {

    var limited = false

    override fun access() {

    }

    override fun limitNow() {
        limited = true
    }

    open fun now(): T = throw NotImplementedError("child class does not support this operation")

    override fun limit(): String {
        return end.toString()
    }

    override fun shouldLimit(): Boolean = false
}
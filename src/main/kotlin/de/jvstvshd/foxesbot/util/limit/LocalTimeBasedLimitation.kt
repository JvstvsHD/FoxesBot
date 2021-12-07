package de.jvstvshd.foxesbot.util.limit

import java.time.LocalTime

class LocalTimeBasedLimitation(override val end: LocalTime) : TemporalBasedLimitation<LocalTime>(end) {

    override fun limitExceeded0(): Boolean =
        end.isBefore(now())


    override fun now(): LocalTime = LocalTime.now()

}
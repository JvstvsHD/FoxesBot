package de.jvstvshd.foxesbot.util.limit

import java.time.LocalTime

class LocalTimeBasedLimitation(override val end: LocalTime) : TemporalBasedLimitation<LocalTime>(end) {

    override fun limitExceeded(): Boolean =
        end.isBefore(now())


    override fun now(): LocalTime = LocalTime.now()

}
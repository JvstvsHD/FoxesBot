package de.jvstvshd.foxesbot.util.limit

abstract class NumberBasedLimitation<T : Number>(open val maxCount: T, private val startValue: T) : Limitation {

    var count: T = startValue

    override fun limitNow() {
        count = maxCount
    }

    override fun toString(): String {
        return "NumberBasedLimitation(maxCount=$maxCount, startValue=$startValue, count=$count)"
    }

}
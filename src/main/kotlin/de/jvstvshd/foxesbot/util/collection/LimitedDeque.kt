package de.jvstvshd.foxesbot.util.collection

import de.jvstvshd.foxesbot.util.limit.LimitExceededException
import de.jvstvshd.foxesbot.util.limit.Limitation
import java.util.*

class LimitedDeque<E>(val limitation: Limitation) : LimitedQueue<E>, ArrayDeque<E>() {

    override fun limited(): Boolean =
        limitation.limitExceeded()

    private fun checkLimitation() {
        if (limited())
            throw LimitExceededException("limit ${limitation.limit()} was exceeded.")
    }

    private fun checkAccess() {
        checkLimitation()
        limitation.access()
    }

    override fun pollFirst(): E {
        checkAccess()
        return super.pollFirst()
    }

    override fun pollLast(): E {
        checkAccess()
        return super.pollLast()
    }

    fun pollUnchecked(): E = poll()
}
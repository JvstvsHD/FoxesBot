package de.jvstvshd.chillingfoxes.foxesbot.util.collection

import java.util.*

interface LimitedQueue<E> : Queue<E> {

    fun limited(): Boolean
}
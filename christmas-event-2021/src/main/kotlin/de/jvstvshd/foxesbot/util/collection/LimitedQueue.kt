package de.jvstvshd.foxesbot.util.collection

import java.util.*

interface LimitedQueue<E> : Queue<E> {

    fun limited(): Boolean
}
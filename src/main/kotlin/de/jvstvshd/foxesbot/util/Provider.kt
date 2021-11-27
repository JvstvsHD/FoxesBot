package de.jvstvshd.foxesbot.util

interface Provider<T> {

    fun provide(): T
}
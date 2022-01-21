package de.jvstvshd.chillingfoxes.foxesbot.util

interface Provider<T> {

    fun provide(): T
}
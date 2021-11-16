package de.jvstvshd.foxesbot.utils

interface Provider<T> {

    fun provide(): T
}
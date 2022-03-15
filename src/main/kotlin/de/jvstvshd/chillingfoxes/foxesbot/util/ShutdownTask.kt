package de.jvstvshd.chillingfoxes.foxesbot.util

interface ShutdownTask {

    suspend fun onShutdown()
}
package de.jvstvshd.foxesbot.module.core.music


enum class MusicState(val playable: Boolean) {
    ACTIVATED(true),
    DEACTIVATED(false),
    DELETED(false)
}
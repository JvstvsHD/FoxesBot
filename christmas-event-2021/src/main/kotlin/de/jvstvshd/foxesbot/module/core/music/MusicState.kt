package de.jvstvshd.foxesbot.module.core.music

enum class MusicState(val playable: Boolean, val readableName: String) {
    ACTIVATED(true, "aktiviert"),
    DEACTIVATED(false, "deaktiviert"),
    DELETED(false, "gelöscht (wtf?!)"),
    UNKNOWN(false, "unbekannt (WARUM???)")
}
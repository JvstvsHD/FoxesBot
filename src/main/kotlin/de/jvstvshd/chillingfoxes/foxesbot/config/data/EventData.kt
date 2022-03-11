package de.jvstvshd.chillingfoxes.foxesbot.config.data

import de.jvstvshd.chillingfoxes.foxesbot.module.event.CountdownResetState

@kotlinx.serialization.Serializable
data class EventData(
    var events: List<String> = listOf(),
    var countdownResetState: CountdownResetState = CountdownResetState.HundredsResetState
)

/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.event

enum class Events(val designation: String) {

    COUNTDOWN(COUNTDOWN_EVENT_NAME),
    PROGRESS_BAR("progress_bar");
}

enum class EventState {
    PENDING, ACTIVE, FINISHED
}

enum class EvenParticipantType {

    GLOBAL,
    INDIVIDUAL;
}
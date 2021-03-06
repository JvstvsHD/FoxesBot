/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.status

class StatusType private constructor(val name: String, val translationKey: String) {
    @Suppress("unused")
    companion object {
        private val types = mutableListOf<StatusType>()
        val OPERATIONAL = create("operational", "status.type.operational")
        val MAINTENANCE = create("maintenance", "status.type.maintenance")
        val PARTIAL_OUTAGE = create("partial_outage", "status.type.outage.partial")
        val MAJOR_OUTAGE = create("major_outage", "status.type.outage.major")
        val UNKNOWN = create("unknown", "status.type.unknown")
        private fun create(name: String, translationKey: String) =
            StatusType(name, translationKey).also { types.add(it) }

        fun canBeParsed(name: String) = types.stream().anyMatch { it.name.lowercase() == name }

        fun getByName(name: String): StatusType {
            return types.find { it.name.equals(name, true) } ?: UNKNOWN
        }
    }

    override fun toString(): String {
        return "StatusType(name='$name', translationKey='$translationKey')"
    }


}
/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.status

data class StatusMetaData(
    val name: String,
    val type: StatusType,
    val children: MutableMap<String, StatusMetaData> = mutableMapOf()
) {

    fun hasChildren() = children.isNotEmpty()

    fun operational(): Boolean {
        if (!hasChildren()) {
            return type == StatusType.OPERATIONAL
        }
        for (value in children.values) {
            if (!value.operational()) {
                return false
            }
        }
        return true
    }
}
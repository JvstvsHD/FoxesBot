/*
 * Copyright (c) 2022 JvstvsHD
 * This file is part of the FoxesBot, a discord bot for the Chilling Foxes Discord (https://discord.gg/K5rhddJtyW), which is licensed under the MIT license. The full version is located in the LICENSE file (top level directory)
 */

package de.jvstvshd.chillingfoxes.foxesbot.module.status

data class StatusData(val url: String, val statusMap: MutableMap<String, StatusMetaData>, val iconUrl: String = "") {
    fun isOperational() = statusMap.values.stream().noneMatch { !it.operational() }

    override fun toString(): String {
        val builder = StringBuilder()
        for (mutableEntry in statusMap) {
            builder.append(toString(mutableEntry.toPair())).append("\n")
        }
        return builder.toString()
    }

    private fun toString(pair: Pair<String, StatusMetaData>): String {
        return if (pair.second.hasChildren()) {
            val builder = StringBuilder()
            builder.append(pair.first + ": " + pair.second.type.name + "\n")
            for (child in pair.second.children) {
                builder.append(">" + child.key + ": " + child.value.type.name + "\n")
            }
            builder.toString()
        } else {
            pair.first + ": " + pair.second.type.name
        }
    }
}
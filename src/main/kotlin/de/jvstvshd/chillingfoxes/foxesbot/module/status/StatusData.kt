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
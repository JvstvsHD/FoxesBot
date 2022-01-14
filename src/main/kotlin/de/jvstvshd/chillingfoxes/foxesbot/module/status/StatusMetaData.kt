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
package de.jvstvshd.foxesbot.module.updatetracker.gomme

data class GommeUpdateContainer(
    val url: String,
    val type: GommeUpdateTracker.GommeUpdateType,
    val author: String = "GommeHD.net Team",
    val title: String = "Update"
) {
    override fun toString(): String {
        return "GommeUpdateContainer(url='$url', type=$type, author='$author', title='$title')"
    }
}
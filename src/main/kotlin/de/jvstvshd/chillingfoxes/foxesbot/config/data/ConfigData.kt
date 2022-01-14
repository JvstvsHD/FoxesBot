package de.jvstvshd.chillingfoxes.foxesbot.config.data

data class ConfigData(
    val dataBaseData: DataBaseData = DataBaseData(),
    val baseData: BaseData = BaseData(),
    val offlineCheckerData: OfflineCheckerData = OfflineCheckerData(),
    val eventData: EventData = EventData()
)
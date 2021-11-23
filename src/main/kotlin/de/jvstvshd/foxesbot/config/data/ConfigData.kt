package de.jvstvshd.foxesbot.config.data

data class ConfigData(
    val dataBaseData: DataBaseData = DataBaseData(),
    val baseData: BaseData = BaseData(),
    val offlineCheckerData: OfflineCheckerData = OfflineCheckerData()
)
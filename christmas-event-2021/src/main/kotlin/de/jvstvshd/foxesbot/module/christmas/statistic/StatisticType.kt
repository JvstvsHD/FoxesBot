package de.jvstvshd.foxesbot.module.christmas.statistic

object UserBotMoves : StatisticType("user_bot_moves")

object ThrownSnowballs : StatisticType("thrown_snowballs")

object ThrownSnowballCount : StatisticType("thrown_snowball_count")

open class StatisticType(val name: String)
package de.jvstvshd.foxesbot.config

import com.kotlindiscord.kord.extensions.utils.env
import dev.kord.common.entity.Snowflake

@Deprecated(message = "Outdated since there is a much easier version with Config/ConfigData", replaceWith = ReplaceWith("Configuration#configData"), level = DeprecationLevel.ERROR)
object Configuration {

    val DB_NAME = env("db.name")
    val DB_SERVER_NAME = env("db.server_name")
    val DB_PORT = env("db.port")
    val DB_USER_NAME  = env("db.user_name")
    val DB_PASSWORD = env("db.password")

    val UPDATE_TRACKER_CORE_POOL_SIZE = env("UPDATE_TRACKER_CORE_POOL_SIZE").toInt()
    val BOT_TOKEN = env("BOT_TOKEN")
    val TEST_GUILD = Snowflake(env("TEST_GUILD"))
}
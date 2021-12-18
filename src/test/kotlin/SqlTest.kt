import de.jvstvshd.foxesbot.config.Config
import de.jvstvshd.foxesbot.io.Database
import org.junit.jupiter.api.Test

class SqlTest {

    @Test
    fun test() {
        val config = Config()
        config.load()
        val database = Database(config.configData.dataBaseData)
        val dataSource = database.dataSource
        var winners = ""
        dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT id FROM christmas_stats WHERE type = ? ORDER BY count DESC LIMIT ?;")
                .use {
                    it.setString(1, "thrown_snowballs")
                    it.setInt(2, 3)
                    val rs = it.executeQuery()
                    var count = 1
                    while (rs.next()) {
                        winners += "<@${rs.getLong(1)}>"
                        if (count < 2) {
                            winners += ", "
                        } else if (count == 2) {
                            winners += " und "
                        }
                        count++
                    }
                }
        }
        println(winners)
    }
}
import de.jvstvshd.foxesbot.config.Config
import de.jvstvshd.foxesbot.io.Database
import org.junit.jupiter.api.Test

class SqlTest {

    @Test
    fun test() {
        val config = Config()
        config.load()
        val database = Database(config.configData.dataBaseData)
    }
}
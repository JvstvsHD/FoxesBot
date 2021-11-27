import de.jvstvshd.foxesbot.module.christmas.HpEvent
import org.junit.jupiter.api.Test

class EventTest {
    @Test
    fun testEvents() {
        for (i in 0..100) {
            println(HpEvent.pickRandom().javaClass)
        }
    }
}
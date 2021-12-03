import de.jvstvshd.foxesbot.module.christmas.HpEvent
import de.jvstvshd.foxesbot.module.christmas.SnowballGain
import org.junit.jupiter.api.Test

class EventTest {
    @Test
    fun testEvents() {
        for (i in 0..100) {
            val event = HpEvent.pickRandom()
            println(event.javaClass.name + ": " + (event is SnowballGain))
        }
    }
}
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.time.ExperimentalTime

class TimerTest {

    @OptIn(DelicateCoroutinesApi::class, ExperimentalTime::class)
    @Test
    fun testTimer() {
        startTimer()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun startTimer() {
        runTimer(18, { it in 18..20 }) {
            println("christmas time")
        }
        runTimer(6, { it == 6 }) {
            println("refill")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun runTimer(startHour: Int, predicate: suspend (Int) -> Boolean, callback: suspend () -> Unit) {
        GlobalScope.launch {
            val hour = LocalTime.now().hour
            if (predicate(hour)) {
                callback.invoke()
            }
            val tomorrow = LocalDateTime.now().plusDays(1).withHour(startHour).withMinute(0)
            val delay = Duration.between(LocalDateTime.now(), tomorrow).toMillis()
            delay(delay)
            callback.invoke()
        }
    }
}
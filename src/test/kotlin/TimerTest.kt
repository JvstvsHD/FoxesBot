import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import java.time.LocalTime
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

class TimerTest {

    @OptIn(DelicateCoroutinesApi::class, ExperimentalTime::class)
    @Test
    fun testTimer() {
        runBlocking {
            GlobalScope.launch {
                val delay = (60 - LocalTime.now().second)
                println("delay = $delay")
                delay(delay.seconds)
                while (true) {
                    val hour = LocalTime.now().minute
                    println("hour = $hour")
                    if (hour == 6) {
                        println("refill")
                    } else if (hour in 18..20) {
                        println("weihnachtstime")
                    }
                    delay(60.seconds)
                }
            }.join()
        }
    }
}
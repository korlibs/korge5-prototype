package korlibs.ffi

import korlibs.io.async.delay
import korlibs.io.async.launchImmediately
import korlibs.template.suspendTest
import korlibs.time.milliseconds
import kotlinx.coroutines.Deferred
import kotlin.test.Test

class FFILibTest {
    object LibC : FFILib("CoreFoundation", "c") {
        val sleep: (Int) -> Deferred<Unit> by func()
    }

    @Test
    fun test() = suspendTest {
        launchImmediately {
            println("1")
            delay(10.milliseconds)
            println("2")
        }
        println("[a]")
        LibC.sleep(1).await()
        println("[b]")
    }
}
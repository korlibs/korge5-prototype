@file:Suppress("PackageDirectoryMismatch")

package korlibs.time.internal

import korlibs.time.*
import korlibs.time.hr.*
import kotlinx.browser.*
import kotlin.time.*

private external val process: dynamic

private val isNode = jsTypeOf(window) == "undefined"
private val initialHrTime: dynamic by lazy { process.hrtime() }

internal actual object KlockInternal {
    actual val currentTime: Double get() = (js("Date.now()").unsafeCast<Double>())

    actual val now: TimeSpan
        get() = when {
        isNode -> {
            val result: Array<Double> = process.hrtime(initialHrTime).unsafeCast<Array<Double>>()
            Duration.fromSeconds(result[0]) + TimeSpan.fromNanoseconds(result[1])
        }
        else -> {
            TimeSpan.fromMilliseconds(window.performance.now())
        }
    }

    actual fun localTimezoneOffsetMinutes(time: DateTime): TimeSpan {
        @Suppress("UNUSED_VARIABLE")
        val rtime = time.unixMillisDouble
        return js("-(new Date(rtime)).getTimezoneOffset()").unsafeCast<Int>().minutes
    }

    actual fun sleep(time: TimeSpan) {
        spinlock(time)
    }
}

actual interface Serializable

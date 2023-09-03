package korlibs.io.async

import korlibs.time.*
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

//suspend fun delay(time: TimeSpan): Unit = kotlinx.coroutines.delay(time.millisecondsLong)
suspend fun CoroutineContext.delay(time: TimeSpan) = kotlinx.coroutines.delay(time.millisecondsLong)

suspend fun <T> withTimeoutOrNil(time: TimeSpan, block: suspend CoroutineScope.() -> T): T {
	return if (time.isNil) {
		block(CoroutineScope(coroutineContext))
	} else {
		kotlinx.coroutines.withTimeout(time.millisecondsLong, block)
	}
}

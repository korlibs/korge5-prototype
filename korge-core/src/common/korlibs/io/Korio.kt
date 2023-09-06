package korlibs.io

import korlibs.io.async.*
import korlibs.io.worker.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

fun Korio(entry: suspend CoroutineScope.() -> Unit) {
    Worker.init {
        asyncEntryPoint { entry(CoroutineScope(coroutineContext)) }
    }
}

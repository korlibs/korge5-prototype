package korlibs.io

import korlibs.io.async.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

fun Korio(entry: suspend CoroutineScope.() -> Unit) = asyncEntryPoint { entry(CoroutineScope(coroutineContext)) }

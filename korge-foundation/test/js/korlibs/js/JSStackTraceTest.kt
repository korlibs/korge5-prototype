package korlibs.js

import kotlin.test.*

class JSStackTraceTest {
    @Test
    fun test() {
        println(JSStackTrace.parse("Error\n    at <anonymous>:1:1"))
        //'Error\n    at <anonymous>:1:1'
        println(JSStackTrace.current())
    }
}

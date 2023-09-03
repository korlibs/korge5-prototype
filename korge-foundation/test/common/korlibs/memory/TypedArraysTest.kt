package korlibs.memory

import kotlin.test.Test
import kotlin.test.assertEquals

class TypedArraysTest {
    @Test
    fun test() {
        val data = Int8Array(10)
        for (n in 0 until 10) data[n] = (1 + n).toByte()
        for (n in 0 until 10) assertEquals((1 + n).toByte(), data[n])
        assertEquals(0, data.byteOffset)
        assertEquals(10, data.byteLength)
        assertEquals(10, data.length)
    }
}
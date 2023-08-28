package korlibs.ffi

import korlibs.crypto.encoding.fromBase64
import kotlin.test.Test
import kotlin.test.assertEquals

class WASMLibTest {
    object SimpleWASM : WASMLib("AGFzbQEAAAABBwFgAn9/AX8DAgEABAUBcAEBAQUDAQAABhQDfwBBCAt/AUGIgAILfwBBiIACCwcQAgNzdW0AAAZtZW1vcnkCAAkGAQBBAQsACgoBCAAgACABag8L".fromBase64()) {
        val sum: (Int, Int) -> Int by func()
        init { finalize() }
    }

    object SimpleWASM2 : WASMLib("AGFzbQEAAAABBwFgAn9/AX8DAgEABAUBcAEBAQUDAQAABhQDfwBBCAt/AUGIgAILfwBBiIACCwcQAgNzdW0AAAZtZW1vcnkCAAkGAQBBAQsACgoBCAAgACABag8L".fromBase64()) {
        val sum: (Int, Int) -> Int by func()
        init { finalize() }
    }

    @Test
    fun test() {
        assertEquals(3, SimpleWASM.sum(1, 2))
        assertEquals(3, SimpleWASM2.sum(1, 2))
        assertEquals(5, SimpleWASM.sum(2, 3))
        assertEquals(5, SimpleWASM2.sum(2, 3))
        //val ptr = SimpleWASM.allocBytes(byteArrayOf(0, 10, 20, 30))
        //SimpleWASM.freeBytes(ptr)
        //assertEquals(listOf(10, 20), SimpleWASM.readBytes(ptr + 1, 2).map { it.toInt() }.toList())
    }
}
package korlibs.ffi

import korlibs.encoding.fromBase64
import kotlin.test.Test
import kotlin.test.assertEquals

class WASMLibTest {
    object SimpleWASM : WASMLib("AGFzbQEAAAABBwFgAn9/AX8DAgEABAUBcAEBAQUDAQAABhQDfwBBCAt/AUGIgAILfwBBiIACCwcQAgNzdW0AAAZtZW1vcnkCAAkGAQBBAQsACgoBCAAgACABag8L".fromBase64()) {
        val sum: (Int, Int) -> Int by func()
    }

    object SimpleWASM2 : WASMLib("AGFzbQEAAAABBwFgAn9/AX8DAgEABAUBcAEBAQUDAQAABhQDfwBBCAt/AUGIgAILfwBBiIACCwcQAgNzdW0AAAZtZW1vcnkCAAkGAQBBAQsACgoBCAAgACABag8L".fromBase64()) {
        val sum: (Int, Int) -> Int by func()
    }


    object SimpleWASM4 : WASMLib("AGFzbQEAAAABDwNgAn9/AX9gAABgAX8BfwMFBAEAAAIEBQFwAQQEBQYBAYACgAIHSgYGbWVtb3J5AgADYWRkAAIDc3ViAAEHZ2V0RnVuYwADGV9faW5kaXJlY3RfZnVuY3Rpb25fdGFibGUBAAtfaW5pdGlhbGl6ZQAACQkBAEEBCwMBAgAKHgQCAAsHACAAIAFrCwcAIAAgAWoLCQBBAUECIAAbCw==".fromBase64()) {
        val sum: (a: Int, b: Int) -> Int by func()
        val sub: (a: Int, b: Int) -> Int by func()
        val getFunc: (type: Int) -> Int by func()
        /*
        // emcc demo.c -Oz -s STANDALONE_WASM --no-entry
        //#include <stdio.h>
        #include <emscripten.h>
        typedef int (*Binop)(int a, int b);
        EMSCRIPTEN_KEEPALIVE int add(int a, int b) { return a + b; }
        EMSCRIPTEN_KEEPALIVE int sub(int a, int b) { return a - b; }
        EMSCRIPTEN_KEEPALIVE Binop getFunc(int type) { return (type == 0) ? add : sub; }
         */
    }


    @Test
    fun test() {
        assertEquals(3, SimpleWASM.sum(1, 2))
        assertEquals(3, SimpleWASM2.sum(1, 2))
        assertEquals(5, SimpleWASM.sum(2, 3))
        assertEquals(5, SimpleWASM2.sum(2, 3))
        //println(SimpleWASM2.getFunc(0, 0))
        //val ptr = SimpleWASM.allocBytes(byteArrayOf(0, 10, 20, 30))
        //SimpleWASM.freeBytes(ptr)
        //assertEquals(listOf(10, 20), SimpleWASM.readBytes(ptr + 1, 2).map { it.toInt() }.toList())
    }

    object LibC : FFILib("CoreFoundation") {
        val dlopen: (String, Int) -> FFIPointer by func()
        val dlsym: (FFIPointer, String) -> FFIPointer by func()
    }

    @Test
    fun test2() {
        val funcPtr0 = SimpleWASM4.getFunc(0)
        val funcPtr1 = SimpleWASM4.getFunc(1)
        val func0 = SimpleWASM4.funcPointer<(Int, Int) -> Int>(funcPtr0)
        val func1 = SimpleWASM4.funcPointer<(Int, Int) -> Int>(funcPtr1)
        assertEquals(10, func0(3, 7))
        assertEquals(-4, func1(3, 7))
    }
}

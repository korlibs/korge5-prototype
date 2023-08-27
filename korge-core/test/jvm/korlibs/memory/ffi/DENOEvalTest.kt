package korlibs.memory.ffi

import korlibs.image.bitmap.Bitmap32
import korlibs.inject.util.suspendTest
import korlibs.io.file.VfsFile
import korlibs.io.file.std.resourcesVfs
import korlibs.memory.Buffer
import korlibs.memory.getArrayInt32
import korlibs.memory.getInt32
import korlibs.time.measureTime
import kotlin.test.Test

class DENOEvalTest {
    class WebpWASM(bytes: ByteArray) : WASMLib(bytes) {
        companion object {
            suspend operator fun invoke(file: VfsFile): WebpWASM = WebpWASM(file.readBytes())
        }
        val decode: (data: Int, size: Int, widthPtr: Int, heightPtr: Int) -> Int by func()
        val get_info: (data: Int, size: Int) -> Int by func()
        init { finalize() }

        fun decodeWebpBytes(bytes: ByteArray): Bitmap32 {
            val memTemp = malloc(16)
            val ptr = allocBytes(bytes)
            val ptr2 = decode(ptr, bytes.size, memTemp, memTemp + 4)
            val buffer = Buffer(readBytes(memTemp, 8))
            val width = buffer.getInt32(0)
            val height = buffer.getInt32(1)
            //Console.log("width", width)
            //Console.log("height", height)
            val pixels = IntArray(width * height)
            Buffer(readBytes(ptr2, width * height * 4)).getArrayInt32(0, pixels)
            //memory.getUnalignedArrayInt32(ptr2, pixels)

            freeBytes(memTemp, ptr, ptr2)

            return Bitmap32(width, height, pixels)
        }
    }
    @Test
    fun test() = suspendTest {
        val wasmBytes = resourcesVfs["webp.wasm"].readBytes()
        /*

        val time = measureTime {
            val demo = DenoWasmSocket()
            demo.loadWASM(wasmBytes)
            for (n in 0 until 100) {
                val time2 = measureTime {
                    val ptr = demo.allocAndWrite(wasmBytes)
                    println("  ptr=$ptr")
                    demo.free(ptr)
                }
                println("time=$time2")
            }
            demo.unloadWASM()
        }
        println("totalTime=$time")
        */

        val wasm = WebpWASM(wasmBytes)
        val webpBytes = resourcesVfs["Exif5-2x.webp"].readBytes()
        for (n in 0 until 100) {
            println(measureTime { wasm.decodeWebpBytes(webpBytes) })
            //println("image=$image")
        }

        /*
        val eval = DENOEval()
        val module = eval.loadWASM(wasmBytes)
        println("webpId: $module")
        module.writeBytes(2, byteArrayOf(1, 2, 3, 4))
        println("malloc: " + module.evalWASMFunction("malloc", 10))
        println("readMem: " + module.readBytes(3, 10).toList())
         */
    }
}
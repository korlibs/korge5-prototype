import korlibs.crypto.encoding.fromBase64
import korlibs.image.bitmap.Bitmap32
import korlibs.image.color.Colors
import korlibs.image.format.readBitmap
import korlibs.io.file.VfsFile
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.Korge
import korlibs.korge.input.keys
import korlibs.korge.tween.get
import korlibs.korge.tween.tween
import korlibs.korge.view.image
import korlibs.korge.view.solidRect
import korlibs.korge.view.xy
import korlibs.math.geom.Size
import korlibs.memory.ffi.WASMLib
import korlibs.memory.getUnalignedArrayInt32
import korlibs.memory.getUnalignedInt32
import korlibs.time.seconds

//suspend fun main() {
//    val DEBUG_FONT_BYTES: ByteArray = "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".fromBase64()
//    println(FFICoreGraphicsImageFormatProvider.decode(DEBUG_FONT_BYTES))
//}

object SimpleWASM : WASMLib("AGFzbQEAAAABBwFgAn9/AX8DAgEABAUBcAEBAQUDAQAABhQDfwBBCAt/AUGIgAILfwBBiIACCwcQAgNzdW0AAAZtZW1vcnkCAAkGAQBBAQsACgoBCAAgACABag8L".fromBase64()) {
    val sum: (Int, Int) -> Int by func()
    init { finalize() }
}

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
        val width = memory.getUnalignedInt32(memTemp)
        val height = memory.getUnalignedInt32(memTemp + 4)
        //Console.log("width", width)
        //Console.log("height", height)
        val pixels = IntArray(width * height)
        memory.getUnalignedArrayInt32(ptr2, pixels)

        free(memTemp)
        free(ptr)
        free(ptr2)

        return Bitmap32(width, height, pixels)
    }
}

suspend fun main() = Korge {
    val wasm = WebpWASM(resourcesVfs["webp.wasm"])
    val webpImage = wasm.decodeWebpBytes(resourcesVfs["Exif5-2x.webp"].readBytes())

    image(resourcesVfs["Exif5-2x.avif"].readBitmap()).xy(300, 0)
    image(webpImage).xy(300, 300)
    solidRect(Size(100, 100), Colors.RED)
    val rect = solidRect(Size(100, 100), Colors.BLUEVIOLET).xy(200, 0)
    keys {
        down { println("KeyDown: $it") }
        up { println("KeyUp: $it") }
    }
    //val sound = resourcesVfs["demo.mp3"].readSound()

    while (true) {
        //sound.play()
        tween(rect::x[100.0], time = 1.seconds)
        tween(rect::x[200.0], time = 1.seconds)
    }

    //println()
    //image(Bitmap32(64, 64, Colors.RED.premultiplied))
}

/*
suspend fun main() {
    println(localCurrentDirVfs.list().toList())
    println(localCurrentDirVfs["build.gradle.kts"].stat())
    println(localCurrentDirVfs["build.gradle.kts2"].stat())
}
*/

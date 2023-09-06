import korlibs.audio.format.MP3
import korlibs.audio.sound.readSound
import korlibs.audio.sound.toSound
import korlibs.encoding.fromBase64
import korlibs.ffi.*
import korlibs.image.format.*
import korlibs.io.async.delay
import korlibs.io.async.launchImmediately
import korlibs.io.file.std.resourcesVfs
import korlibs.io.lang.*
import korlibs.io.worker.*
import korlibs.korge.Korge
import korlibs.korge.view.*
import korlibs.time.*
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext
import kotlin.js.*

//suspend fun main() {
//    val DEBUG_FONT_BYTES: ByteArray = "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".fromBase64()
//    println(FFICoreGraphicsImageFormatProvider.decode(DEBUG_FONT_BYTES))
//}

object SimpleWASM : WASMLib("AGFzbQEAAAABBwFgAn9/AX8DAgEABAUBcAEBAQUDAQAABhQDfwBBCAt/AUGIgAILfwBBiIACCwcQAgNzdW0AAAZtZW1vcnkCAAkGAQBBAQsACgoBCAAgACABag8L".fromBase64()) {
    val sum: (Int, Int) -> Int by func()
}

object LibC : FFILib("CoreFoundation") {
    val dlopen: (String, Int) -> FFIPointer by func()
    val dlsym: (FFIPointer, String) -> FFIPointer by func()
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

object SimpleWASM3 : WASMLib("AGFzbQEAAAABGgRgAn9/AX9gAX8Bf2AEf39/fwBgA39/fwF/Ag0BA2VudgVhYm9ydAACAwUEAAABAwQEAXAAAwUDAQABBggBfwFBzIgCCwcvBgNzdW0AAQNzdWIAAgdnZXRGdW5jAAMGbWVtb3J5AgAFdGFibGUBAARleGVjAAQJCAEAQQELAgECDAEECl0EBwAgACABagsHACAAIAFrCwsAQcAIQaAIIAAbCz8AIwBBBGskACMAQcwISARAQeCIAkGQiQJBAUEBEAAACyMAIAA2AgAgASACIAAoAgARAAAhACMAQQRqJAAgAAsLLQQAQYwICwEcAEGYCAsJBAAAAAgAAAABAEGsCAsBHABBuAgLCQQAAAAIAAAAAg==".fromBase64()) {
    val sum: (a: Int, b: Int) -> Int by func()
    val sub: (a: Int, b: Int) -> Int by func()
    val getFunc: (type: Int) -> Int by func()
    /*
    // AssemblyScript
    // sudo npm -g install assemblyscript
    // asc sum.asc.ts --outFile sum.wasm --optimize

    type Callback = (a: i32, b: i32) => i32;

    export function sum(a: i32, b: i32): i32 {
      return a + b;
    }

    export function sub(a: i32, b: i32): i32 {
        return a - b;
      }

    export function getFunc(type: i32): Callback {
        if (type == 0) {
            return sum
        } else {
            return sub
        }
    }
    */
}

object LibC2 : FFILib("CoreFoundation", "c") {
    val sleep: (Int) -> Deferred<Unit> by func()
}

suspend fun main4() {
    launchImmediately(coroutineContext) {
        println("1")
        delay(10.milliseconds)
        println("2")
    }
    println("[a]")
    LibC2.sleep(10).await()
    println("[b]")

    //val data = MP3.decode(resourcesVfs["demo.mp3"].readBytes())
    //data?.toSound()?.play()
    //println("data=$data")
    //SDL.SDL_InitSubSystem(0x00000020)
    //SDL.SDL_GL_LoadLibrary(null)
    //println("ADDRESS: " + SDL.SDL_GL_GetProcAddress("glEnable")?.address)

    //val bytes = resourcesVfs["Exif5-2x.avif"].readBytes()
    //val bytes = resourcesVfs["bubble-chat.9.png"].readBytes()

    //println(nativeImageFormatProvider.decode(bytes))
    //resourcesVfs["Exif5-2x.avif"].readBitmap()

    //val funcPtr0 = SimpleWASM4.getFunc(0)
    //val funcPtr1 = SimpleWASM4.getFunc(1)
    //val func0 = SimpleWASM4.funcPointer<(Int, Int) -> Int>(funcPtr0)
    //val func1 = SimpleWASM4.funcPointer<(Int, Int) -> Int>(funcPtr1)
    //println("func=$funcPtr0")
    //println(func0(3, 7))
    //println(func1(3, 7))

    //val lib = LibC.dlopen("/System/Library/Frameworks/CoreFoundation.framework/CoreFoundation", 0)
    //println("lib=${lib.address}")
    //val sym = LibC.dlsym(lib, "dlopen")
    //println("sym=${sym.address}")
    //val cos = LibC.dlsym(lib, "cos").castToFunc<(Double) -> Double>()
    //println(cos(0.5))
}

@JsExport
class MyTask : WorkerTask() {
    //companion object {
    //    init {
    //        Worker.register(MyTask::class)
    //    }
    //}
    override fun execute() = runSuspend {
        println("TEST!!!!! $params")
        //delay(1.seconds)
        //error("ERROR!")
        return@runSuspend 41
    }
}

suspend fun main() = Korge {
    //run { val bytes = resourcesVfs["Exif5-2x.webp"].readBytes(); for (n in 0 until 100) println(measureTime { WEBP.decode(bytes) }) }
    //image(WEBP.decode(resourcesVfs["Exif5-2x.webp"]))
    image(resourcesVfs["Exif5-2x.webp"].readBitmap(WEBP))
    println(runCatching {
        image(resourcesVfs["Exif5-2x.avif"].readBitmap()).xy(100, 100)
    }.exceptionOrNull())
    //val data = MP3.decode(resourcesVfs["demo.mp3"].readBytes())
    val sound = resourcesVfs["demo.mp3"].readSound()
    sound?.play()
    //data?.toSound()?.play()

    val worker = Worker()
    //delay(1.seconds)
    val result = worker.execute<MyTask>("hello", "world", byteArrayOf(1, 2, 3, 4))
    println("RESULT=$result")
    val result2 = worker.execute<DemoWorkerTask>("hello", "world", byteArrayOf(1, 2, 3, 4))
    println("RESULT=$result2")

    worker.close()


    /*
    //val WEBP = WEBPImageFormat(resourcesVfs["webp.wasm"])
    val WEBP = WEBPImageFormat()

    println(resourcesVfs["Exif5-2x.webp"].readImageInfo(WEBP.toProps()))
    println(resourcesVfs["Exif5-2x.avif"].readImageInfo(WEBP.toProps()))
    image(resourcesVfs["Exif5-2x.webp"].readImageData(WEBP.toProps()).mainBitmap)

     */
    return@Korge
    /*

    val avifBytes = resourcesVfs["Exif5-2x.avif"].readBytes()
    val webpBytes = resourcesVfs["Exif5-2x.webp"].readBytes()
    run {
        val wasm = WebpWASM(resourcesVfs["webp.wasm"])
        println("AVIF: " + wasm.getInfo(avifBytes.sliceArray(0 until 100)))
        println("WEBP: " + wasm.getInfo(webpBytes.sliceArray(0 until 100)))
    }
    val wasm = WebpWASM(resourcesVfs["webp.wasm"])
    val webpImage = wasm.decodeWebpBytes(resourcesVfs["Exif5-2x.webp"].readBytes())

    val avifImage = nativeImageFormatProvider.decode(avifBytes)
    image(avifImage).xy(300, 0)
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

     */
}

/*
suspend fun main() {
    println(localCurrentDirVfs.list().toList())
    println(localCurrentDirVfs["build.gradle.kts"].stat())
    println(localCurrentDirVfs["build.gradle.kts2"].stat())
}
*/

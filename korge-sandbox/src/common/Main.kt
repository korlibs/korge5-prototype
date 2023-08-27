import korlibs.audio.sound.AudioTone
import korlibs.audio.sound.backends.ffi.AL
import korlibs.audio.sound.backends.ffi.FFIOpenALNativeSoundProvider
import korlibs.audio.sound.playAndWait
import korlibs.audio.sound.readSound
import korlibs.audio.sound.toStream
import korlibs.crypto.encoding.fromBase64
import korlibs.image.bitmap.Bitmap32
import korlibs.image.color.Colors
import korlibs.image.format.providers.FFICoreGraphicsImageFormatProvider
import korlibs.io.async.delay
import korlibs.io.file.std.localCurrentDirVfs
import korlibs.io.file.std.tempVfs
import korlibs.korge.Korge
import korlibs.korge.bitmapfont.debugBmpFont
import korlibs.korge.input.keys
import korlibs.korge.tween.get
import korlibs.korge.tween.tween
import korlibs.korge.view.image
import korlibs.korge.view.solidRect
import korlibs.korge.view.xy
import korlibs.math.geom.Size
import korlibs.memory.Platform
import korlibs.memory.ffi.FFILib
import korlibs.memory.ffi.getStringz
import korlibs.render.ffi.sdl.SDL
import korlibs.render.ffi.sdl.SDL_WINDOWPOS_CENTERED
import korlibs.render.ffi.sdl.SDL_WINDOW_OPENGL
import korlibs.render.ffi.sdl.SDL_WINDOW_RESIZABLE
import korlibs.time.milliseconds
import korlibs.time.seconds

//suspend fun main() {
//    val DEBUG_FONT_BYTES: ByteArray = "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".fromBase64()
//    println(FFICoreGraphicsImageFormatProvider.decode(DEBUG_FONT_BYTES))
//}

val DEBUG_FONT_BYTES: ByteArray = "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".fromBase64()

suspend fun main() = Korge {
    /*
    solidRect(Size(100, 100), Colors.RED)
    val rect = solidRect(Size(100, 100), Colors.GREEN).xy(200, 0)
    keys {
        down { println("KeyDown: $it") }
        up { println("KeyUp: $it") }
    }
    val sound = tempVfs["demo.mp3"].readSound()

    while (true) {
        sound.play()
        tween(rect::x[100.0], time = 1.seconds)
        tween(rect::x[200.0], time = 1.seconds)
    }

     */
    //println()
    //image(Bitmap32(64, 64, Colors.RED.premultiplied))
    image(FFICoreGraphicsImageFormatProvider.decode(DEBUG_FONT_BYTES))
}

/*
suspend fun main() {
    println(localCurrentDirVfs.list().toList())
    println(localCurrentDirVfs["build.gradle.kts"].stat())
    println(localCurrentDirVfs["build.gradle.kts2"].stat())
}
*/

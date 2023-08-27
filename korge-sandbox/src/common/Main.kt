import korlibs.korge.Korge
import korlibs.memory.ffi.FFILib

object LibC : FFILib("/System/Library/Frameworks/CoreFoundation.framework/CoreFoundation") {
    val cos by func<(value: Double) -> Double>()
    val cosf by func<(value: Float) -> Float>()
    init {
        finalize()
    }
}

suspend fun main() = Korge {
    println("-----------------")
    println(LibC.cos(.5))
    println(LibC.cosf(.5f))
    println("+++++++++++++++++")
    /*
    println("STARTED!")
    solidRect(Size(100, 100), Colors.RED)
    val rect = solidRect(Size(100, 100), Colors.GREEN).xy(200, 0)
    keys {
        down { println("KeyDown: $it") }
        up { println("KeyUp: $it") }
    }
    while (true) {
        tween(rect::x[100.0], time = 1.seconds)
        tween(rect::x[200.0], time = 1.seconds)
    }

     */
}

/*
suspend fun main() {
    println(localCurrentDirVfs.list().toList())
    println(localCurrentDirVfs["build.gradle.kts"].stat())
    println(localCurrentDirVfs["build.gradle.kts2"].stat())
}
*/

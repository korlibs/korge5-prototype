import korlibs.image.color.Colors
import korlibs.io.file.std.localCurrentDirVfs
import korlibs.korge.Korge
import korlibs.korge.input.keys
import korlibs.korge.tween.get
import korlibs.korge.tween.tween
import korlibs.korge.view.solidRect
import korlibs.korge.view.xy
import korlibs.math.geom.Size
import korlibs.time.seconds
import kotlinx.coroutines.flow.toList

suspend fun main() = Korge {
    println("STARTED!")
    solidRect(Size(100, 100), Colors.RED)
    val rect = solidRect(Size(100, 100), Colors.GREEN).xy(200, 0)
    keys {
        down {
            println("KeyDown: $it")
        }
    }
    tween(rect::x[100.0], time = 2.seconds)
}

/*
suspend fun main() {
    println(localCurrentDirVfs.list().toList())
    println(localCurrentDirVfs["build.gradle.kts"].stat())
    println(localCurrentDirVfs["build.gradle.kts2"].stat())
}
*/

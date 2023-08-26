package korlibs.korge.view

import korlibs.korge.tests.*
import korlibs.korge.ui.*
import korlibs.image.color.*
import korlibs.math.geom.*
import korlibs.math.geom.vector.*
import kotlin.test.*

class ViewHitTestTest : ViewsForTesting() {
    @Test
    fun testShape() = viewsTest{
        val circleB = solidRect(128.0, 128.0, Colors.RED).anchor(Anchor.MIDDLE_CENTER)
            .position(256, 256)
            .hitShape { circle(Point(64, 64), 64f) }

        assertEquals(true, circleB.hitTestAny(Point(256, 256)))
        assertEquals(true, circleB.hitTestAny(Point(200, 256)))
        assertEquals(true, circleB.hitTestAny(Point(300, 213)))
        assertEquals(false, circleB.hitTestAny(Point(306, 205)))
    }

    @Test
    fun test() = viewsTest{
        val circleB = solidRect(128.0, 128.0, Colors.RED).anchor(Anchor.MIDDLE_CENTER)
            .position(256, 256)

        assertEquals(true, circleB.hitTestAny(Point(256, 256)))
        assertEquals(true, circleB.hitTestAny(Point(200, 256)))
        assertEquals(true, circleB.hitTestAny(Point(300, 213)))
        assertEquals(true, circleB.hitTestAny(Point(306, 205)))
        assertEquals(false, circleB.hitTestAny(Point(322, 205)))
    }
}


inline fun Container.polygon(
    radius: Double = 16.0,
    sides: Int = 5,
    color: RGBA = Colors.WHITE,
    autoScaling: Boolean = true,
    callback: Polygon.() -> Unit = {}
): Polygon = Polygon(radius, sides, color, autoScaling).addTo(this, callback)

class Polygon(
    radius: Double = 16.0,
    sides: Int = 5,
    color: RGBA = Colors.WHITE,
    autoScaling: Boolean = true
) : CpuGraphics(autoScaling = autoScaling) {
    /** Radius of the circle */
    var radius: Double by uiObservable(radius) { updateGraphics() }

    /** Number of sides of the polygon */
    var sides: Int by uiObservable(sides) { updateGraphics() }

    /** Color of the circle. Internally it uses the [colorMul] property */
    var color: RGBA
        get() = colorMul
        set(value) { colorMul = value }

    //override val bwidth get() = radius * 2
    //override val bheight get() = radius * 2

    init {
        this.color = color
        updateGraphics()
    }

    private fun updateGraphics() {
        val polygon = this
        updateShape {
            fill(Colors.WHITE) {
                for (n in 0 until polygon.sides) {
                    val angle = ((360.degrees * n) / polygon.sides) - 90.degrees
                    val x = polygon.radius * angle.cosineD
                    val y = polygon.radius * angle.sineD
                    //println("$x, $y")
                    if (n == 0) {
                        moveTo(Point(x, y))
                    } else {
                        lineTo(Point(x, y))
                    }
                }
                close()
            }
        }
    }
}

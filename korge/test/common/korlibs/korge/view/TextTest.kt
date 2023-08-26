package korlibs.korge.view

import assertEqualsFloat
import korlibs.image.font.*
import korlibs.math.geom.*
import kotlin.test.*

class TextTest {
    @Test
    fun testTextBounds() {
        val text = Text("", font = DefaultTtfFont)
        assertEqualsFloat(
            Rectangle.fromBounds(Point(0, 0), Point(0, 18.4)),
            text.getLocalBounds().roundDecimalPlaces(1)
        )
        text.text = "hello"
        assertEqualsFloat(
            Rectangle.fromBounds(Point(-0.8, 0), Point(28.9, 18.4)),
            text.getLocalBounds().roundDecimalPlaces(1)
        )
    }
}

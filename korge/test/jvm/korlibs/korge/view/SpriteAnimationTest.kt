package korlibs.korge.view

import korlibs.time.*
import korlibs.image.atlas.*
import korlibs.io.async.*
import korlibs.io.file.std.*
import kotlin.test.*

class SpriteAnimationTest {
    @Test
    fun test() = suspendTest {
        val animation = resourcesVfs["atlas/spritesheet.json"].readAtlas().getSpriteAnimation("RunRight", 100.milliseconds)
        assertEquals(4, animation.size)
        assertEquals("RunRight01.png", animation[0].name)
        assertEquals("RunRight02.png", animation[1].name)
        assertEquals("RunRight03.png", animation[2].name)
        assertEquals("RunRight04.png", animation[3].name)
    }

    @Test
    fun test2() = suspendTest {
        val atlas = resourcesVfs["atlas/sheet.xml"].readAtlas()
        val animation = atlas.getSpriteAnimation(Regex("beam\\d+.png"), 100.milliseconds)
        assertEquals(1024, atlas.texture.width)
        assertEquals(1024, atlas.texture.height)
        assertEquals(7, animation.size)
        assertEquals("beam0.png", animation[0].name)
        assertEquals("beam6.png", animation[6].name)
    }

    @Test
    fun testDifferentXMLStyle() = suspendTest {
        val atlas = resourcesVfs["atlas/adventurer.xml"].readAtlas()
        val animation = atlas.getSpriteAnimation("run", 100.milliseconds)
        assertEquals(100, atlas.texture.width)
        assertEquals(2035, atlas.texture.height)
        assertEquals(109, atlas.entries.size)
        assertEquals(6, animation.size)
    }
}

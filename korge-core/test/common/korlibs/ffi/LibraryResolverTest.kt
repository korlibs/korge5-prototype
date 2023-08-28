package korlibs.memory.ffi

import korlibs.ffi.LibraryResolver
import korlibs.io.file.sync.MemorySyncIO
import korlibs.io.file.sync.file
import korlibs.memory.Os
import korlibs.memory.Platform
import kotlin.test.Test
import kotlin.test.assertEquals

class LibraryResolverTest {
    @Test
    fun testMacos() {
        val fs = MemorySyncIO()
        val resolver = LibraryResolver(fs, Platform(os = Os.MACOSX))
        fs.file("/Library/Frameworks/SDL2.framework").mkdir()

        assertEquals("/Library/Frameworks/SDL2.framework/SDL2", resolver.resolve("SDL2"))
    }

    @Test
    fun testLinux() {
        val fs = MemorySyncIO().also { it.writeLinuxLD() }
        val resolver = LibraryResolver(fs, Platform(os = Os.LINUX))

        fs.file("/usr/local/lib/aarch64-linux-gnu/libSDL2.so").writeString("dummy")

        assertEquals("/usr/local/lib/aarch64-linux-gnu/libSDL2.so", resolver.resolve("SDL2"))
    }

    @Test
    fun testLinuxDiffuse() {
        val fs = MemorySyncIO().also { it.writeLinuxLD() }
        val resolver = LibraryResolver(fs, Platform(os = Os.LINUX))

        fs.file("/usr/local/lib/aarch64-linux-gnu/libSDL2.2.0.0.so").writeString("dummy")

        assertEquals("/usr/local/lib/aarch64-linux-gnu/libSDL2.2.0.0.so", resolver.resolve("SDL2"))
    }

    private fun MemorySyncIO.writeLinuxLD() {
        file("/Library/Frameworks/SDL2.framework").mkdir()

        file("/lib").mkdir()
        file("/usr/lib").mkdir()
        file("/lib/aarch64-linux-gnu").mkdir()

        file("/etc/ld.so.conf").writeString("include /etc/ld.so.conf.d/*.conf\n")
        file("/etc/ld.so.conf.d/aarch64-linux-gnu.conf").writeString("# Multiarch support\n/usr/local/lib/aarch64-linux-gnu\n/lib/aarch64-linux-gnu\n/usr/lib/aarch64-linux-gnu\n")
        file("/etc/ld.so.conf.d/fakeroot-aarch64-linux-gnu.conf").writeString("/usr/lib/aarch64-linux-gnu/libfakeroot\n")
        file("/etc/ld.so.conf.d/libc.conf").writeString("# libc default configuration\n/usr/local/lib\n")
    }
}
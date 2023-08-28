package korlibs.memory.ffi

import korlibs.io.file.sync.MemorySyncIO
import korlibs.io.file.sync.file
import kotlin.test.Test
import kotlin.test.assertEquals

class LDLibrariesTest {
    @Test
    fun test() {
        val fs = MemorySyncIO()
        val ld = LDLibraries(fs)
        fs.file("/lib").mkdir()
        fs.file("/usr/lib").mkdir()
        fs.file("/usr/local/lib/aarch64-linux-gnu").mkdir()
        fs.file("/lib/aarch64-linux-gnu").mkdir()

        fs.file("/etc/ld.so.conf").writeString("include /etc/ld.so.conf.d/*.conf\n")
        fs.file("/etc/ld.so.conf.d/aarch64-linux-gnu.conf").writeString("# Multiarch support\n/usr/local/lib/aarch64-linux-gnu\n/lib/aarch64-linux-gnu\n/usr/lib/aarch64-linux-gnu\n")
        fs.file("/etc/ld.so.conf.d/fakeroot-aarch64-linux-gnu.conf").writeString("/usr/lib/aarch64-linux-gnu/libfakeroot\n")
        fs.file("/etc/ld.so.conf.d/libc.conf").writeString("# libc default configuration\n/usr/local/lib\n")

        //ld.hasLibrary()
        assertEquals(
            listOf("/lib", "/usr/lib", "/usr/local/lib/aarch64-linux-gnu", "/lib/aarch64-linux-gnu", "/usr/local/lib"),
            ld.ldFolders.map { it.fullPath }
        )
    }
}
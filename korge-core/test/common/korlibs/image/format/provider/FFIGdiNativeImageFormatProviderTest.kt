package korlibs.image.format.provider

import korlibs.io.file.std.resourcesVfs
import korlibs.memory.Platform
import korlibs.template.suspendTest
import kotlin.test.Test

class FFIGdiNativeImageFormatProviderTest {
    @Test
    fun test() = suspendTest {
        if (Platform.isWindows) {
            FFIGdiNativeImageFormatProvider.decode(resourcesVfs["bubble-chat.9.png"].readBytes())
        }
        //FFIGdiNativeImageFormatProvider.decode(resourcesVfs["Exif5-2x.avif"].readBytes())
    }
}
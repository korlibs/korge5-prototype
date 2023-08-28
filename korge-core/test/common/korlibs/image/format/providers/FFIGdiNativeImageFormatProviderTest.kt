package korlibs.image.format.providers

import korlibs.image.format.*
import korlibs.io.file.std.*
import korlibs.template.*
import kotlin.test.*

class FFIGdiNativeImageFormatProviderTest {
    @Test
    fun test() = suspendTest {
        FFIGdiNativeImageFormatProvider.decode(resourcesVfs["bubble-chat.9.png"].readBytes()).showImageAndWait()
        //FFIGdiNativeImageFormatProvider.decode(resourcesVfs["Exif5-2x.avif"].readBytes())
    }
}
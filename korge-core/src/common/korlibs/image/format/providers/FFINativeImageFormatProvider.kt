package korlibs.image.format.providers

import korlibs.image.format.*
import korlibs.memory.*

val FFINativeImageFormatProviderOpt: BaseNativeImageFormatProvider? by lazy {
    when {
        Platform.isMac -> FFICoreGraphicsImageFormatProvider
        Platform.isWindows -> FFIGdiNativeImageFormatProvider
        else -> null
    }
}

val FFINativeImageFormatProvider get() = FFINativeImageFormatProviderOpt
    ?: error("Unsupported platform decodeHeaderInternal '${Platform.os}'")

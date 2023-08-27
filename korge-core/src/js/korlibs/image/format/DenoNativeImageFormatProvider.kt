package korlibs.image.format

import korlibs.image.bitmap.Bitmap32
import korlibs.io.jsObject
import korlibs.io.runtime.deno.Deno
import korlibs.io.runtime.deno.DenoPointer
import korlibs.io.runtime.deno.def
import korlibs.math.geom.SizeInt
import korlibs.memory.TypedBuffer
import org.khronos.webgl.*

/*
external interface DenoCoreFoundation {
    fun CFDataCreate(allocator: DenoPointer?, buffer: ByteArray, size: Int): DenoPointer?
    fun CFStringCreateWithBytes(ptr: DenoPointer?, buffer: ByteArray, numBytes: Int, encoding: Int, isExternalRepresentation: Int): DenoPointer?
    fun CFDictionaryGetValue(ptr: DenoPointer?, key: DenoPointer?): DenoPointer?
    fun CFNumberGetValue(ptr: DenoPointer?, theType: Int, valuePtr: IntArray): Boolean
    fun CFDataGetBytePtr(ptr: DenoPointer?): DenoPointer?
    fun CFDataGetLength(ptr: DenoPointer?): Int
    fun memcpy(dst: ByteArray?, src: DenoPointer?, len: Int): DenoPointer?
    fun memcpy(dst: IntArray?, src: DenoPointer?, len: Int): DenoPointer?
    fun CFRelease(ptr: DenoPointer?): Unit
}

external interface DenoCoreGraphics {
}

external interface DenoImageIO {
}
*/

private val cf = Deno.dlopen<dynamic>(
    "/System/Library/Frameworks/CoreFoundation.framework/CoreFoundation",
    jsObject(
        "CFDataCreate" to def("pointer", "pointer", "buffer", "usize"),
        "CFStringCreateWithBytes" to def("pointer", "pointer", "buffer", "usize", "usize", "u8"),
        "CFDictionaryGetValue" to def("pointer", "pointer", "pointer"),
        "CFNumberGetValue" to def("i8", "pointer", "i32", "buffer"),
        "CFDataGetBytePtr" to def("buffer", "pointer"),
        "CFDataGetLength" to def("usize", "pointer"),
        "memcpy" to def("pointer", "buffer", "pointer", "usize"),
        "CFRelease" to def("void", "pointer"),
    )
).symbols

private val CGFloat = "f64"
private val CGRectStruct = jsObject("struct" to arrayOf(CGFloat, CGFloat, CGFloat, CGFloat))

/*
interface DenoFFITypes {
    val USIZE get() = "usize" to Int::class
    val POINTER get() = "pointer" to DenoPointer::class
    val BYTEARRAY get() = "buffer" to ByteArray::class
}

class DenoLibrary(val libPath: String) {
    val funcs = arrayListOf<FuncRef<*>>()

    val symbols: dynamic by lazy {
        Deno.dlopen<dynamic>(
            libPath,
            jsObject(
                *funcs
                    .map { it.funcName to def(clazzToName(it.ret), *it.args.map { clazzToName(it) }.toTypedArray()) }
                    .toTypedArray(),
            )
        ).symbols
    }

    fun clazzToName(clazz: KClass<*>): String {
        return when (clazz) {
            DenoPointer::class -> "pointer"
            ByteArray::class -> "buffer"
            IntArray::class -> "buffer"
            Int::class -> "usize"
            else -> TODO("$clazz")
        }
    }

    inner class LazyFunc<T>(val funcName: String): ReadOnlyProperty<Any, T> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T {
            //console.log("symbols", symbols, "funcName", funcName, "res", symbols[funcName])
            return symbols[funcName]
        }
    }

    inner class FuncRef<U : Any>(val ret: KClass<*>, vararg val args: KClass<*>) {
        var funcName: String = ""
        operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ReadOnlyProperty<Any, U> {
            funcName = property.name
            return LazyFunc<U>(property.name).also { funcs.add(this) }
        }
    }

    inline fun <reified TR> func0(): FuncRef<() -> TR> = FuncRef(TR::class)
    inline fun <reified TR, reified T1> func1(): FuncRef<(T1) -> TR> = FuncRef(TR::class, T1::class)
    inline fun <reified TR, reified T1, reified T2> func2(): FuncRef<(T1, T2) -> TR> = FuncRef(TR::class, T1::class, T2::class)
    inline fun <reified TR, reified T1, reified T2, reified T3> func3(): FuncRef<(T1, T2, T3) -> TR> = FuncRef(TR::class, T1::class, T2::class, T3::class)
    inline fun <reified TR, reified T1, reified T2, reified T3, reified T4> func4(): FuncRef<(T1, T2, T3, T4) -> TR> = FuncRef(TR::class, T1::class, T2::class, T3::class, T4::class)
    inline fun <reified TR, reified T1, reified T2, reified T3, reified T4, reified T5> func5(): FuncRef<(T1, T2, T3, T4, T5) -> TR> = FuncRef(TR::class, T1::class, T2::class, T3::class, T4::class, T5::class)
    inline fun <reified TR, reified T1, reified T2, reified T3, reified T4, reified T5, reified T6> func6(): FuncRef<(T1, T2, T3, T4, T5, T6) -> TR> = FuncRef(TR::class, T1::class, T2::class, T3::class, T4::class, T5::class, T6::class)
    inline fun <reified TR, reified T1, reified T2, reified T3, reified T4, reified T5, reified T6, reified T7> func7(): FuncRef<(T1, T2, T3, T4, T5, T6, T7) -> TR> = FuncRef(TR::class, T1::class, T2::class, T3::class, T4::class, T5::class, T6::class, T7::class)
}

object DenoCoreGraphics : DenoFFITypes {
    val lib = DenoLibrary("/System/Library/Frameworks/CoreGraphics.framework/CoreGraphics")
    //val CFDataCreate by lib.func(POINTER, BYTEARRAY, USIZE, ret = POINTER)
    //val CFDataCreate by lib.func<DenoPointer, ByteArray, Int>()
    //val CGImageGetWidth by lib.func(POINTER, ret = USIZE)
    val CFDataCreate by lib.func3<DenoPointer?, DenoPointer?, ByteArray, Int>()

    fun test() {
        CFDataCreate(null, byteArrayOf(1, 2, 3), 3)
    }
}
*/

private val cg = Deno.dlopen<dynamic>(
    "/System/Library/Frameworks/CoreGraphics.framework/CoreGraphics",
    jsObject(
        "CGImageGetWidth" to def("usize", "pointer"),
        "CGImageGetHeight" to def("usize", "pointer"),
        "CGColorSpaceCreateDeviceRGB" to def("pointer"),
        "CGBitmapContextCreate" to def("pointer", "buffer", "usize", "usize", "usize", "usize", "pointer", "i32"),
        "CGImageRelease" to def("void", "pointer"),
        "CGContextRelease" to def("void", "pointer"),
        "CGColorSpaceRelease" to def("void", "pointer"),
        "CGContextFlush" to def("void", "pointer"),
        "CGContextDrawImage" to def("void", "pointer", CGRectStruct, "pointer")
    //        "CGRectMake": def("pointer", "f32", "f32", "f32", "f32"),
    )
).symbols

private val iio = Deno.dlopen<dynamic>(
    "/System/Library/Frameworks/ImageIO.framework/ImageIO",
    jsObject(
        "CGImageSourceCreateWithData" to def("pointer", "pointer", "pointer"),
        "CGImageSourceCopyPropertiesAtIndex" to def("pointer", "pointer", "usize", "pointer"),
        "CGImageSourceCreateImageAtIndex" to def("pointer", "pointer", "i32", "pointer"),
    )
).symbols

private fun getIntFromDict(props: DenoPointer?, key: String): Int {
    val kCFNumberIntType = 9
    val buffer = IntArray(2)
    val keyPtr = createCFString(key)
    cf.CFNumberGetValue(cf.CFDictionaryGetValue(props, keyPtr), kCFNumberIntType, buffer)
    cf.CFRelease(keyPtr)
    return buffer[0]
}

private fun createCFString(str: String): DenoPointer? {
    val NSUTF8StringEncoding = 4
    val bytes = str.encodeToByteArray()//TextEncoder().encode(str)
    return cf.CFStringCreateWithBytes(null, bytes, bytes.size, NSUTF8StringEncoding, 0)
}


private fun getImageSize(bytes: ByteArray): SizeInt {
    val data = cf.CFDataCreate(null, bytes, bytes.size)
    val imgSource = iio.CGImageSourceCreateWithData(data, null)
    val props = iio.CGImageSourceCopyPropertiesAtIndex(imgSource, 0, null)
    val width = getIntFromDict(props, "PixelWidth")
    val height = getIntFromDict(props, "PixelHeight")
    cf.CFRelease(props)
    cf.CFRelease(imgSource)
    cf.CFRelease(data)
    return SizeInt(width, height)

}

private fun CGRectMake(x: Double, y: Double, width: Double, height: Double): DoubleArray {
    return doubleArrayOf(x, y, width, height)
}

private fun getImageData(bytes: ByteArray): Pair<IntArray, SizeInt> {
    //console.log(readPointer(dataPtr, dataLen));
    val premultiplied = true
    val data = cf.CFDataCreate(null, bytes, bytes.size)
    val imgSource = iio.CGImageSourceCreateWithData(data, null)
    val dict = null
    val cgImage = iio.CGImageSourceCreateImageAtIndex(imgSource, 0, dict)
    val width: Int = cg.CGImageGetWidth(cgImage).unsafeCast<Double>().toInt()
    val height: Int = cg.CGImageGetHeight(cgImage).unsafeCast<Double>().toInt()
    val colorSpace = cg.CGColorSpaceCreateDeviceRGB()
    val alphaInfo = if (premultiplied) 1 else 3

    val pixels = IntArray(width * height)

    val context = cg.CGBitmapContextCreate(pixels, width, height, 8, width * 4, colorSpace, alphaInfo)
    val rect = CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble())
    cg.CGContextDrawImage(context, rect, cgImage)
    cg.CGContextFlush(context)

    cg.CGImageRelease(cgImage)
    cg.CGContextRelease(context)
    cf.CFRelease(imgSource)
    cf.CFRelease(data)
    cg.CGColorSpaceRelease(colorSpace)

//    const data = CGDataProviderCopyData(CGImageGetDataProvider(cgImage))
    return pixels to SizeInt(width, height)
}

object DenoNativeImageFormatProvider : BaseNativeImageFormatProvider() {
    override val formats: ImageFormat get() = RegisteredImageFormats
    override suspend fun encodeSuspend(image: ImageDataContainer, props: ImageEncodingProps): ByteArray {
        //Deno.ffi()
        return RegisteredImageFormats.formats.first().encode(image.default)
        //return PNG.encode(image.default.mainBitmap)
    }

    override suspend fun decodeHeaderInternal(data: ByteArray): ImageInfo {
        when (val os = Deno.build.os) {
            "darwin" -> {
                val size = getImageSize(data)
                return ImageInfo {width = size.width; height = size.height}
            }
            else -> {
                error("Unsupported Deno '$os'")
            }
        }
    }

    override suspend fun decodeInternal(data: ByteArray, props: ImageDecodingProps): NativeImageResult {
        when (val os = Deno.build.os) {
            "darwin" -> {
                val (data, size) = getImageData(data)
                return NativeImageResult(
                    BitmapNativeImage(Bitmap32(size.width, size.height, data))
                )
            }
            else -> {
                error("Unsupported Deno '$os'")
            }
        }
    }

    //init { RegisteredImageFormats.register(PNG) }
}

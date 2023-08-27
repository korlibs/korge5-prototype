package korlibs.memory.ffi

import korlibs.image.bitmap.NativeImage
import korlibs.io.jsObject
import korlibs.io.runtime.deno.Deno
import korlibs.io.runtime.deno.DenoPointer
import korlibs.io.runtime.deno.def
import korlibs.memory.Buffer
import kotlin.reflect.KClassifier

fun KClassifier.toDenoFFI(ret: Boolean): dynamic {
    return when (this) {
        Long::class -> "usize"
        Int::class -> "i32"
        Float::class -> "f32"
        Double::class -> "f64"
        Boolean::class -> "i8"
        NativeImage::class -> "buffer"
        ByteArray::class -> "buffer"
        IntArray::class -> "buffer"
        Buffer::class -> "buffer"
        DenoPointer::class -> "pointer"
        String::class -> if (ret) "pointer" else "buffer"
        Unit::class -> "void"
        else -> TODO("$this")
    }
}

actual class FFILibSym actual constructor(val lib: FFILib) {
    val symbols = jsObject(
        *lib.functions.map {
            it.name to def(it.ret?.toDenoFFI(ret = true), *it.params.map { it?.toDenoFFI(ret = false) }.toTypedArray())
        }.toTypedArray()
    )
    init {
        //console.log("symbols", symbols)
    }
    val syms: dynamic = lib.paths.firstNotNullOfOrNull { path ->
        try {
            Deno.dlopen<dynamic>(path, symbols).symbols
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }
    actual fun <T> get(name: String): T {
        if (syms == null) error("Can't get symbol '$name' for ${lib::class} : '${lib.paths}'")
        return syms[name]
    }
}

actual typealias FFIPointer = DenoPointer

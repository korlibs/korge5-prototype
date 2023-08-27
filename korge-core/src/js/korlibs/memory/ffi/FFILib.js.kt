package korlibs.memory.ffi

import korlibs.image.bitmap.NativeImage
import korlibs.io.jsObject
import korlibs.io.runtime.deno.*
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
        ShortArray::class -> "buffer"
        CharArray::class -> "buffer"
        IntArray::class -> "buffer"
        FloatArray::class -> "buffer"
        DoubleArray::class -> "buffer"
        BooleanArray::class -> "buffer"
        Buffer::class -> "buffer"
        DenoPointer::class -> "pointer"
        String::class -> if (ret) "pointer" else "buffer"
        Unit::class -> "void"
        else -> TODO("$this")
    }
}

actual class FFILibSym actual constructor(val lib: FFILib) {
    val symbolsByName: Map<String, FFILib.FuncDelegate<*>> by lazy { lib.functions.associateBy { it.name } }

    val syms: dynamic by lazy {
        lib.paths.firstNotNullOfOrNull { path ->
            try {
                Deno.dlopen<dynamic>(
                    path, jsObject(
                        *lib.functions.map {
                            it.name to def(
                                it.ret?.toDenoFFI(ret = true),
                                *it.params.map { it?.toDenoFFI(ret = false) }.toTypedArray()
                            )
                        }.toTypedArray()
                    )
                ).symbols
            } catch (e: Throwable) {
                e.printStackTrace()
                null
            }
        }
    }

    fun preprocessFunc(delegate: FFILib.FuncDelegate<*>, func: dynamic): dynamic {
        val convertToString = delegate.ret == String::class
        return {
            val arguments = js("(arguments)")
            //console.log("arguments", arguments)
            val result = func.apply(null, arguments)
            when {
                convertToString -> (result.unsafeCast<DenoPointer>()).getStringz()
                else -> result
            }
        }
    }

    actual fun <T> get(name: String): T {
        if (syms == null) error("Can't get symbol '$name' for ${lib::class} : '${lib.paths}'")
        //return syms[name]
        return preprocessFunc(symbolsByName[name]!!, syms[name])
    }
}

actual typealias FFIPointer = DenoPointer

actual fun FFIPointer.getStringz(): String = this.readStringz()
actual val FFIPointer?.str: String get() = if (this == null) "Pointer(null)" else "Pointer($value)"

actual fun FFIPointer.readInts(size: Int, offset: Int): IntArray {
    val view = Deno.UnsafePointerView(this)
    val out = IntArray(size)
    for (n in 0 until size) {
        out[n] = view.asDynamic().getInt32(offset + n * 4)
    }
    //Deno.UnsafePointerView.getCString()
    //TODO("Not yet implemented")
    return out
}
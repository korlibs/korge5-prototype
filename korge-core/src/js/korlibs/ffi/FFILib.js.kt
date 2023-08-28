package korlibs.ffi

import korlibs.image.bitmap.NativeImage
import korlibs.io.jsObject
import korlibs.io.runtime.deno.*
import korlibs.js.Deno
import korlibs.js.DenoPointer
import korlibs.js.readStringz
import korlibs.js.value
import korlibs.memory.Buffer
import org.khronos.webgl.*
import kotlin.reflect.*

fun KType.toDenoDef(): dynamic {
    val (params, ret) = BaseLib.extractTypeFunc(this)
    return def(
        ret?.toDenoFFI(ret = true),
        *params.map { it?.toDenoFFI(ret = false) }.toTypedArray()
    )
}

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

external class WebAssembly {
    class Instance(module: Module, imports: dynamic) {
        val exports: dynamic
        val memory: ArrayBuffer
    }
    class Module(data: ByteArray)
}

private external val JSON: dynamic

actual fun FFILibSym(lib: BaseLib): FFILibSym {
    return FFILibSymJS(lib)
}

class FFILibSymJS(val lib: BaseLib) : FFILibSym {
    val symbolsByName: Map<String, BaseLib.FuncDelegate<*>> by lazy { lib.functions.associateBy { it.name } }

    private var _wasmExports: dynamic = null

    val wasmExports: dynamic get() {
        if (_wasmExports == null) {
            _wasmExports = try {
                val module = WebAssembly.Module((lib as WASMLib).content!!)
                val imports = jsObject(
                    "wasi_snapshot_preview1" to jsObject(
                        "proc_exit" to { console.log("proc_exit", js("(arguments)")) },
                        "fd_close" to { console.log("fd_close", js("(arguments)")) },
                        "fd_write" to { console.log("fd_write", js("(arguments)")) },
                        "fd_seek" to { console.log("fd_seek", js("(arguments)")) },
                    )
                )
                WebAssembly.Instance(module, imports).exports
            } catch (e: Throwable) {
                e.printStackTrace()
                null
            }.unsafeCast<Any?>().also {
                //println("exports=${JSON.stringify(it)}")
            }
        }
        return _wasmExports
    }

    val mem: ArrayBuffer by lazy {
        wasmExports.memory.buffer
    }
    val u8: Uint8Array by lazy {
        Uint8Array(mem)
    }

    override fun readBytes(pos: Int, size: Int): ByteArray {
        val out = ByteArray(size)
        val u8 = this.u8
        for (n in out.indices) out[n] = u8[pos + n]
        return out
    }

    override fun writeBytes(pos: Int, data: ByteArray) {
        for (n in data.indices) u8[pos + n] = data[n]
    }

    override fun allocBytes(bytes: ByteArray): Int {
        val ptr = wasmExports["malloc"](bytes.size)
        writeBytes(ptr, bytes)
        return ptr
    }
    override fun freeBytes(vararg ptrs: Int) {
        for (ptr in ptrs) if (ptr != 0) wasmExports["free"](ptr)
    }

    val syms: dynamic by lazy {
        lib as FFILib
        (listOfNotNull(lib.resolvedPath) + lib.paths).firstNotNullOfOrNull { path ->
            try {
                Deno.dlopen<dynamic>(
                    path, jsObject(
                        *lib.functions.map {
                            it.name to it.type.toDenoDef()
                        }.toTypedArray()
                    )
                ).symbols
            } catch (e: Throwable) {
                //e.printStackTrace()
                null
            }
        }.unsafeCast<Any?>().also {
            if (it == null) {
                println("Couldn't load library: dymlib=$it : ${lib.resolvedPath}")
            }
        }
    }

    fun preprocessFunc(delegate: BaseLib.FuncDelegate<*>, func: dynamic): dynamic {
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

    val libIsWasm = lib is WASMLib

    override fun <T> get(name: String): T {
        val syms = if (libIsWasm) {
            wasmExports
        } else {
            if (syms == null) error("Can't get symbol '$name' for ${lib::class} : '${(lib as FFILib).paths}'")
            syms
        }
        //return syms[name]
        return preprocessFunc(symbolsByName[name]!!, syms[name])
    }

    override fun close() {
        super.close()
    }

    override fun <T> castToFunc(ptr: FFIPointer?, funcInfo: BaseLib.FuncInfo<T>): T {
        return Deno.UnsafeFnPointer(ptr, funcInfo.type.toDenoDef()).asDynamic()//.call
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
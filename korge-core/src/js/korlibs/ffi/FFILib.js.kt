package korlibs.ffi

import korlibs.image.bitmap.NativeImage
import korlibs.io.jsObject
import korlibs.io.runtime.deno.*
import korlibs.js.*
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
                val dummyFunc = { console.log("proc_exit", js("(arguments)")) }
                val imports = jsObject(
                    "env" to jsObject(
                        "abort" to dummyFunc,
                    ),
                    "wasi_snapshot_preview1" to jsObject(
                        "proc_exit" to dummyFunc,
                        "fd_close" to dummyFunc,
                        "fd_write" to dummyFunc,
                        "fd_seek" to dummyFunc,
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
    val dataView: DataView by lazy {
        DataView(mem)
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

    // @TODO: Optimize this
    fun preprocessFunc(type: KType, func: dynamic): dynamic {
        val (params, ret) = BaseLib.extractTypeFunc(type)
        val convertToString = ret == String::class
        return {
            val arguments = js("(arguments)")
            for (n in 0 until params.size) {
                val param = params[n]
                var v = arguments[n]
                if (param == String::class) {
                   v = (v.toString() + "\u0000").encodeToByteArray()
                }
                arguments[n] = v
            }
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
        return preprocessFunc(symbolsByName[name]!!.type, syms[name])
    }

    override fun <T> wasmFuncPointer(address: Int, type: KType): T {
        if (!wasmExports.table && !wasmExports.__indirect_function_table) {
            console.log("wasmExports", Deno.inspect(wasmExports))
            error("Table not exported with 'table' name")
        }
        val table = wasmExports.__indirect_function_table ?: wasmExports.table

        val func = table.get(address)
        return preprocessFunc(type, func)
    }

    override fun close() {
        super.close()
    }
}

actual typealias FFIPointer = DenoPointer

actual fun FFIPointer.getStringz(): String = this.readStringz()
actual val FFIPointer?.address: Long get() {
    val res = Deno.UnsafePointer.value(this)
    return if (res is Number) res.toLong() else res.unsafeCast<JsBigInt>().toLong()
}
actual fun CreateFFIPointer(ptr: Long): FFIPointer = Deno.UnsafePointer.create(ptr.toJsBigInt())
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

actual fun <T> FFIPointer.castToFunc(type: KType): T {
    val def = type.toDenoDef()
    val res = Deno.UnsafeFnPointer(this, def)
    //console.log("castToFunc.def=", def, "res=", res)
    val func: dynamic = {
        val arguments = js("(arguments)")
        res.asDynamic().call.apply(res, arguments)
    }
    return func

}
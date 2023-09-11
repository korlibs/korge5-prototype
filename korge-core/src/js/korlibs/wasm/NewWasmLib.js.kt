package korlibs.wasm

import korlibs.datastructure.*
import korlibs.ffi.*
import korlibs.io.*
import korlibs.js.*
import korlibs.memory.*
import kotlinx.coroutines.*
import org.khronos.webgl.*
import kotlin.js.*
import kotlin.reflect.*

actual fun WasmSYMLib(lib: NewWASMLib): WasmSYMLib = object : WasmSYMLib {
    val symbolsByName: Map<String, NewWASMLib.FuncDelegate<*>> by lazy { lib.functions.associateBy { it.name } }
    private var _wasmExports: dynamic = null

    val wasmExports: dynamic get() {
        if (_wasmExports == null) {
            _wasmExports = try {
                val module = WebAssembly.Module(lib.content!!)
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
    override fun stackSave(): Int = wasmExports.stackSave()
    override fun stackRestore(ptr: Int): Unit = wasmExports.stackRestore(ptr)
    override fun stackAlloc(size: Int): Int = wasmExports.stackAlloc(size)

    override fun <T> wasmFuncPointer(address: Int, type: KType): T {
        if (!wasmExports.table && !wasmExports.__indirect_function_table) {
            console.log("wasmExports", Deno.inspect(wasmExports))
            error("Table not exported with 'table' name")
        }
        val table = wasmExports.__indirect_function_table ?: wasmExports.table

        val func: Any? = table.get(address)
        return preprocessFunc(type, func, "func\$$address").unsafeCast<T>()
    }

    override fun <T> get(name: String, type: KType): T {
        val syms = wasmExports
        //return syms[name]
        return preprocessFunc(symbolsByName[name]!!.type, syms[name], name)
    }

    override fun close() {
        super.close()
    }

    // @TODO: Optimize this
    // @TODO: This might not be required if we are only using ints, floats, etc.?
    private fun preprocessFunc(type: KType, func: dynamic, name: String?): dynamic {
        val ftype = BaseLib.extractTypeFunc(type)
        val convertToString = ftype.retClass == String::class
        return {
            val arguments = js("(arguments)")
            val params = ftype.paramsClass
            for (n in 0 until params.size) {
                val param = params[n]
                var v = arguments[n]
                if (param == String::class) {
                    v = (v.toString() + "\u0000").encodeToByteArray()
                }
                if (v is FFIPointerArray) v = v.data
                if (v is Buffer) v = v.dataView
                if (v is Boolean) v = if (v) 1 else 0
                if (v is Long) v = (v as Long).toJsBigInt()
                //console.log("param", n, v)
                arguments[n] = v
            }
            //console.log("arguments", arguments)
            try {
                val result = func.apply(null, arguments)
                //console.log("result", result)
                val res2 = when {
                    result == null -> null
                    convertToString -> {
                        val ptr = (result.unsafeCast<DenoPointer>())
                        getCString(ptr)
                    }
                    else -> result
                }
                if (res2 is Promise<*>) {
                    (res2.unsafeCast<Promise<*>>()).asDeferred()
                } else {
                    res2
                }
            } catch (e: dynamic) {
                println("ERROR calling[$name]: $type : ${JsArray.from(arguments).toList()}")
                throw e
            }
        }
    }
}

package korlibs.wasm

import korlibs.io.*
import korlibs.js.*
import org.khronos.webgl.*
import kotlin.reflect.*

actual open class WASMLib actual constructor(content: ByteArray) : BaseWASMLib(content) {
    val symbolsByName: Map<String, FuncDelegate<*>> by lazy { functions.associateBy { it.name } }
    private var _wasmExports: dynamic = null

    val wasmExports: dynamic get() {
        if (_wasmExports == null) {
            _wasmExports = try {
                val module = WebAssembly.Module(content!!)
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

    override fun <T : Function<*>> funcPointer(address: Int, type: KType): T {
        if (!wasmExports.table && !wasmExports.__indirect_function_table) {
            console.log("wasmExports", Deno.inspect(wasmExports))
            error("Table not exported with 'table' name")
        }
        val table = wasmExports.__indirect_function_table ?: wasmExports.table

        val func: Any? = table.get(address)
        return preprocessFunc(type, func, "func\$$address").unsafeCast<T>()
    }

    override fun <T> symGet(name: String, type: KType): T {
        val syms = wasmExports
        //return syms[name]
        return preprocessFunc(symbolsByName[name]!!.type, syms[name], name)
    }

    override fun close() {
        super.close()
    }

    private fun preprocessFunc(type: KType, func: dynamic, name: String?): dynamic {
        return func
    }
}

private external class WebAssembly {
    class Instance(module: Module, imports: dynamic) {
        val exports: dynamic
        val memory: ArrayBuffer
    }
    class Module(data: ByteArray)
}

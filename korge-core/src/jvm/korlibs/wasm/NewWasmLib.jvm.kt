package korlibs.wasm

import com.sun.jna.Function
import korlibs.ffi.*
import kotlin.reflect.*

actual fun WasmSYMLib(lib: NewWASMLib): WasmSYMLib = object : WasmSYMLib {
    val functions: Map<String, kotlin.Function<*>> by lazy {
        lib.functions.associate { nfunc ->
            //val lib = NativeLibrary.getInstance("")
            nfunc.name to createFunction(nfunc.name, nfunc.type)
        }
    }

    fun <T : kotlin.Function<*>> createFunction(funcName: String, type: KType): T {
        return createWasmFunctionToPlainFunction<T>(wasm, funcName, type)
    }

    val wasm: DenoWASM by lazy {
        DenoWasmProcessStdin.open(lib.content)
    }

    override fun readBytes(pos: Int, size: Int): ByteArray = wasm.readBytes(pos, size)
    override fun writeBytes(pos: Int, data: ByteArray) = wasm.writeBytes(pos, data)
    override fun allocBytes(bytes: ByteArray): Int = wasm.allocAndWrite(bytes)
    override fun freeBytes(vararg ptrs: Int) = wasm.free(*ptrs)
    override fun stackSave(): Int = wasm.stackSave()
    override fun stackRestore(ptr: Int) = wasm.stackRestore(ptr)
    override fun stackAlloc(size: Int): Int = wasm.stackAlloc(size)
    override fun stackAllocAndWrite(bytes: ByteArray): Int = wasm.stackAllocAndWrite(bytes)

    override fun <T> wasmFuncPointer(address: Int, type: KType): T =
        createWasmFunctionToPlainFunctionIndirect(wasm, address, type)

    override fun <T> get(name: String, type: KType): T {
        return functions[name] as T
    }

    override fun close() {
        wasm.close()
    }
}

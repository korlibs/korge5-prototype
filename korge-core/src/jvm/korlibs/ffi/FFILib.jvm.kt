package korlibs.ffi

import com.sun.jna.Function
import com.sun.jna.Memory
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer
import korlibs.datastructure.lock.Lock
import korlibs.io.file.sync.*
import korlibs.io.serialization.json.Json
import korlibs.memory.*
import korlibs.wasm.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Proxy
import java.util.concurrent.Executors
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

actual fun FFILibSym(lib: BaseLib): FFILibSym {
    return FFILibSymJVM(lib)
}

actual typealias FFIPointer = Pointer
actual typealias FFIMemory = Memory

actual fun CreateFFIMemory(size: Int): FFIMemory = Memory(size.toLong())
actual fun CreateFFIMemory(bytes: ByteArray): FFIMemory = Memory(bytes.size.toLong()).also { it.write(0L, bytes, 0, bytes.size) }
actual val FFIMemory.pointer: FFIPointer get() = this

@JvmName("FFIPointerCreation")
actual fun CreateFFIPointer(ptr: Long): FFIPointer? = if (ptr == 0L) null else Pointer(ptr)
actual val FFI_POINTER_SIZE: Int = 8

actual fun FFIPointer.getStringz(): String = this.getString(0L)
actual val FFIPointer?.address: Long get() = Pointer.nativeValue(this)
actual val FFIPointer?.str: String get() = this.toString()
actual fun FFIPointer.getIntArray(size: Int, offset: Int): IntArray = this.getIntArray(0L, size)

actual fun FFIPointer.getUnalignedI8(offset: Int): Byte = this.getByte(offset.toLong())
actual fun FFIPointer.getUnalignedI16(offset: Int): Short = this.getShort(offset.toLong())
actual fun FFIPointer.getUnalignedI32(offset: Int): Int = this.getInt(offset.toLong())
actual fun FFIPointer.getUnalignedI64(offset: Int): Long = this.getLong(offset.toLong())
actual fun FFIPointer.getUnalignedF32(offset: Int): Float = this.getFloat(offset.toLong())
actual fun FFIPointer.getUnalignedF64(offset: Int): Double = this.getDouble(offset.toLong())

actual fun FFIPointer.setUnalignedI8(value: Byte, offset: Int) = this.setByte(offset.toLong(), value)
actual fun FFIPointer.setUnalignedI16(value: Short, offset: Int) = this.setShort(offset.toLong(), value)
actual fun FFIPointer.setUnalignedI32(value: Int, offset: Int) = this.setInt(offset.toLong(), value)
actual fun FFIPointer.setUnalignedI64(value: Long, offset: Int) = this.setLong(offset.toLong(), value)
actual fun FFIPointer.setUnalignedF32(value: Float, offset: Int) = this.setFloat(offset.toLong(), value)
actual fun FFIPointer.setUnalignedF64(value: Double, offset: Int) = this.setDouble(offset.toLong(), value)

actual fun <T> FFIPointer.castToFunc(type: KType): T =
    createJNAFunctionToPlainFunc(Function.getFunction(this), type)

fun <T : kotlin.Function<*>> createJNAFunctionToPlainFunc(func: Function, type: KType): T {
    val ftype = BaseLib.extractTypeFunc(type)

    return Proxy.newProxyInstance(
        FFILibSymJVM::class.java.classLoader,
        arrayOf((type.classifier as KClass<*>).java)
    ) { proxy, method, args ->
        val targs = (args ?: emptyArray()).map {
            when (it) {
                is FFIPointerArray -> it.data
                is Buffer -> it.buffer
                else -> it
            }
        }.toTypedArray()

        var ret = ftype.retClass
        val isDeferred = ret == Deferred::class
        if (isDeferred) {
            ret = ftype.ret?.arguments?.first()?.type?.classifier
        }

        fun call(): Any? {
            return when (ret) {
                Unit::class -> func.invokeVoid(targs)
                Int::class -> func.invokeInt(targs)
                Float::class -> func.invokeFloat(targs)
                Double::class -> func.invokeDouble(targs)
                else -> func.invoke((ftype.retClass as KClass<*>).java, targs)
            }
        }

        if (isDeferred) {
            CompletableDeferred<Any?>().also { value ->
                Executors.newCachedThreadPool().execute { value.complete(call()) }
                //value.complete(call())
            }
        } else {
            call()
        }
    } as T
}

fun <T : kotlin.Function<*>> createWasmFunctionToPlainFunction(wasm: DenoWASM, funcName: String, type: KType): T {
    val ftype = BaseLib.extractTypeFunc(type)
    return Proxy.newProxyInstance(
        FFILibSymJVM::class.java.classLoader,
        arrayOf((type.classifier as KClass<*>).java)
    ) { proxy, method, args ->
        val sargs = args ?: emptyArray()
        //return wasm.evalWASMFunction(nfunc.name, *sargs)
        wasm.executeFunction(funcName, *sargs)
    } as T
}

fun <T : kotlin.Function<*>> createWasmFunctionToPlainFunctionIndirect(wasm: DenoWASM, address: Int, type: KType): T {
    val ftype = BaseLib.extractTypeFunc(type)
    return Proxy.newProxyInstance(
        FFILibSymJVM::class.java.classLoader,
        arrayOf((type.classifier as KClass<*>).java)
    ) { proxy, method, args ->
        val sargs = args ?: emptyArray()
        //return wasm.evalWASMFunction(nfunc.name, *sargs)
        wasm.executeFunctionIndirect(address, *sargs)
    } as T
}

inline fun <reified T : kotlin.Function<*>> createJNAFunctionToPlainFunc(func: Function): T =
    createJNAFunctionToPlainFunc(func, typeOf<T>())

class FFILibSymJVM(val lib: BaseLib) : FFILibSym {
    @OptIn(SyncIOAPI::class)
    val nlib by lazy {
        lib as FFILib
        val resolvedPaths = listOf(LibraryResolver.resolve(*lib.paths.toTypedArray()))
        resolvedPaths.firstNotNullOfOrNull {
            NativeLibrary.getInstance(it)
        }
    }

    val libWasm = lib as? WASMLib?

    fun <T : kotlin.Function<*>> createFunction(funcName: String, type: KType): T {
        return if (libWasm != null) {
            createWasmFunctionToPlainFunction<T>(wasm, funcName, type)
        } else {
            val func: Function = nlib!!.getFunction(funcName) ?: error("Can't find function ${funcName}")
            createJNAFunctionToPlainFunc<T>(func, type)
        }
    }

    val functions: Map<String, kotlin.Function<*>> by lazy {
        lib.functions.associate { nfunc ->
            //val lib = NativeLibrary.getInstance("")
            nfunc.name to createFunction(nfunc.name, nfunc.type)
        }
    }

    val wasm: DenoWASM by lazy {
        if (libWasm == null) error("Not a WASM module")
        DenoWasmProcessStdin.open(libWasm.content)
    }

    override fun <T> get(name: String, type: KType): T = functions[name] as T
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

    override fun close() {
        if (libWasm != null) {
            wasm.close()
        }
    }
}

package korlibs.wasm

import korlibs.datastructure.*
import korlibs.io.lang.*
import korlibs.memory.*
import kotlin.properties.*
import kotlin.reflect.*

open class NewWASMLib(val content: ByteArray) {
    val lazyCreate = true
    val functions = arrayListOf<FuncDelegate<*>>()
    //val loaded: Boolean get() = sym != null
    val loaded: Boolean get() = true
    val sym by lazy { WasmSYMLib(this) }

    companion object {
        class FuncType(val params: List<KType?>, val ret: KType?) {
            val paramsClass = params.map { it?.classifier }
            val retClass = ret?.classifier
        }

        fun extractTypeFunc(type: KType): FuncType {
            val generics = type.arguments.map { it.type }
            val ret = generics.last()
            val params = generics.dropLast(1)
            return FuncType(params, ret)
        }
    }

    class FuncDelegate<T>(val base: NewWASMLib, val name: String, val type: KType) : ReadOnlyProperty<NewWASMLib, T> {
        val parts = extractTypeFunc(type)
        //val generics = type.arguments.map { it.type?.classifier }
        val params = parts.paramsClass
        val ret = parts.retClass
        var cached: T? = null
        override fun getValue(thisRef: NewWASMLib, property: KProperty<*>): T {
            if (cached == null) cached = base.sym.get(name, type)
            return cached.fastCastTo()
        }
    }

    class FuncInfo<T>(val type: KType, val extraName: String?) {
        operator fun provideDelegate(
            thisRef: NewWASMLib,
            prop: KProperty<*>
        ): ReadOnlyProperty<NewWASMLib, T> = FuncDelegate<T>(thisRef, extraName ?: prop.name, type).also {
            thisRef.functions.add(it)
            if (!thisRef.lazyCreate) it.getValue(thisRef, prop)
        }
    }

    inline fun <reified T : Function<*>> func(name: String? = null): FuncInfo<T> = FuncInfo<T>(typeOf<T>(), name)

    //inline fun <reified T : Function<*>> castToFunc(ptr: FFIPointer): T = sym.castToFunc(ptr, FuncInfo(typeOf<T>(), null))
    protected fun finalize() {
    }

    //val memory: Buffer by lazy { sym.memory }

    val malloc: (Int) -> Int by func()
    val free: (Int) -> Unit by func()

    inline fun <T> stackKeep(block: () -> T): T {
        val ptr = stackSave()
        try {
            return block()
        } finally {
            stackRestore(ptr)
        }
    }

    fun readBytes(pos: Int, size: Int): ByteArray {
        val out = sym.readBytes(pos, size)
        check(out.size == size) { "${out.size} == $size" }
        return out
    }
    fun readShorts(pos: Int, size: Int): ShortArray {
        val bytes = readBytes(pos, size * 2)
        return ShortArray(size) { bytes.readS16LE(it * 2).toShort() }
    }
    fun readInts(pos: Int, size: Int): IntArray {
        val bytes = readBytes(pos, size * 4)
        return IntArray(size) { bytes.readS32LE(it * 4) }
    }

    fun writeBytes(pos: Int, data: ByteArray) = sym.writeBytes(pos, data)
    fun writeShorts(pos: Int, data: ShortArray) = writeBytes(pos, data.toByteArray())
    fun writeInts(pos: Int, data: IntArray) = writeBytes(pos, data.toByteArray())

    fun ShortArray.toByteArray(): ByteArray = ByteArray(this.size * 2).also { out ->
        for (n in 0 until this.size) out.write16LE(n * 2, this[n].toInt())
    }
    fun IntArray.toByteArray(): ByteArray = ByteArray(this.size * 4).also { out ->
        for (n in 0 until this.size) out.write16LE(n * 4, this[n].toInt())
    }

    fun allocBytes(bytes: ByteArray): Int {
        return sym.allocBytes(bytes)
        //val ptr = malloc(bytes.size)
        //writeBytes(ptr, bytes)
        //memory.setArrayInt8(ptr, bytes)
        //return ptr
    }
    fun freeBytes(vararg ptrs: Int) {
        sym.freeBytes(*ptrs)
    }

    //val stackSave: () -> Int by func()
    //val stackRestore: (ptr: Int) -> Unit by func()
    //val stackAlloc: (size: Int) -> Int by func()

    fun stackSave(): Int = sym.stackSave()
    fun stackRestore(ptr: Int): Unit = sym.stackRestore(ptr)
    fun stackAlloc(size: Int): Int = sym.stackAlloc(size)
    fun stackAllocAndWrite(bytes: ByteArray): Int = sym.stackAllocAndWrite(bytes)
    fun stackAllocAndWrite(data: ShortArray): Int = sym.stackAllocAndWrite(data.toByteArray())
    fun stackAllocAndWrite(data: IntArray): Int = sym.stackAllocAndWrite(data.toByteArray())

    fun <T : Function<*>> funcPointer(address: Int, type: KType): T = sym.wasmFuncPointer(address, type)

    inline fun <reified T : Function<*>> funcPointer(address: Int): T = funcPointer(address, typeOf<T>())
}

expect fun WasmSYMLib(lib: NewWASMLib): WasmSYMLib

interface WasmSYMLib : Closeable {
    fun readBytes(pos: Int, size: Int): ByteArray = TODO()
    fun writeBytes(pos: Int, data: ByteArray): Unit = TODO()
    fun allocBytes(bytes: ByteArray): Int = TODO()
    fun freeBytes(vararg ptrs: Int): Unit = TODO()

    fun stackSave(): Int = TODO()
    fun stackRestore(ptr: Int): Unit = TODO()
    fun stackAlloc(size: Int): Int = TODO()
    fun stackAllocAndWrite(bytes: ByteArray): Int = stackAlloc(bytes.size).also { writeBytes(it, bytes) }

    fun <T> wasmFuncPointer(address: Int, type: KType): T = TODO()
    fun <T> get(name: String, type: KType): T = TODO()
    override fun close() {}
}

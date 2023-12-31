package korlibs.ffi

import korlibs.datastructure.fastCastTo
import korlibs.io.file.sync.SyncIOAPI
import korlibs.io.lang.Closeable
import korlibs.math.*
import korlibs.memory.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.*

expect class FFIPointer

expect class FFIMemory

expect fun CreateFFIMemory(size: Int): FFIMemory
expect fun CreateFFIMemory(bytes: ByteArray): FFIMemory

expect val FFIMemory.pointer: FFIPointer

expect fun CreateFFIPointer(ptr: Long): FFIPointer?
expect val FFIPointer?.address: Long
expect val FFIPointer?.str: String
expect fun FFIPointer.getStringz(): String
expect fun <T> FFIPointer.castToFunc(type: KType): T
inline fun <reified T> FFIPointer.castToFunc(): T = castToFunc(typeOf<T>())
expect val FFI_POINTER_SIZE: Int
expect fun FFIPointer.getIntArray(size: Int, offset: Int = 0): IntArray
expect fun FFIPointer.getUnalignedI8(offset: Int = 0): Byte
expect fun FFIPointer.getUnalignedI16(offset: Int = 0): Short
expect fun FFIPointer.getUnalignedI32(offset: Int = 0): Int
expect fun FFIPointer.getUnalignedI64(offset: Int = 0): Long
expect fun FFIPointer.getUnalignedF32(offset: Int = 0): Float
expect fun FFIPointer.getUnalignedF64(offset: Int = 0): Double
expect fun FFIPointer.setUnalignedI8(value: Byte, offset: Int = 0)
expect fun FFIPointer.setUnalignedI16(value: Short, offset: Int = 0)
expect fun FFIPointer.setUnalignedI32(value: Int, offset: Int = 0)
expect fun FFIPointer.setUnalignedI64(value: Long, offset: Int = 0)
expect fun FFIPointer.setUnalignedF32(value: Float, offset: Int = 0)
expect fun FFIPointer.setUnalignedF64(value: Double, offset: Int = 0)

fun FFIPointer.setUnalignedFFIPointer(value: FFIPointer?, offset: Int = 0) {
    if (FFI_POINTER_SIZE == 8) setUnalignedI64(value.address, offset) else setUnalignedI32(value.address.toInt(), offset)
}

fun FFIPointer.getUnalignedFFIPointer(offset: Int = 0): FFIPointer? =
    if (FFI_POINTER_SIZE == 8) CreateFFIPointer(getUnalignedI64(offset)) else CreateFFIPointer(getUnalignedI32(offset).toLong())

fun FFIPointer.getI8(offset: Int = 0): Byte = getUnalignedI8(offset * 1)
fun FFIPointer.getI16(offset: Int = 0): Short = getUnalignedI16(offset * 2)
fun FFIPointer.getI32(offset: Int = 0): Int = getUnalignedI32(offset * 4)
fun FFIPointer.getI64(offset: Int = 0): Long = getUnalignedI64(offset * 8)
fun FFIPointer.getF32(offset: Int = 0): Float = getUnalignedF32(offset * 4)
fun FFIPointer.getF64(offset: Int = 0): Double = getUnalignedF64(offset * 8)

fun FFIPointer.getFFIPointer(offset: Int = 0): FFIPointer? =
    if (FFI_POINTER_SIZE == 8) CreateFFIPointer(getI64(offset)) else CreateFFIPointer(getI32(offset).toLong())

fun ffiPointerArrayOf(vararg pointers: FFIPointer?): FFIPointerArray = FFIPointerArray(pointers.size).also {
    for (n in pointers.indices) it[n] = pointers[n]
}

@Suppress("ReplaceSizeZeroCheckWithIsEmpty")
data class FFIPointerArray(val data: IntArray) : List<FFIPointer?> {
    constructor(size: Int) : this(IntArray(size * 2))
    override operator fun get(index: Int): FFIPointer? {
        val address = Long.fromLowHigh(data[index * 2 + 0], data[index * 2 + 1])
        if (address == 0L) return null
        return CreateFFIPointer(address)
    }
    operator fun set(index: Int, value: FFIPointer?) {
        val address = value.address
        data[index * 2 + 0] = address.low
        data[index * 2 + 1] = address.high
    }
    override val size: Int get () = data.size / 2
    override fun isEmpty(): Boolean = size == 0

    private val vlist get() = (0 until size).map { this[it] }

    override fun iterator(): Iterator<FFIPointer?> = vlist.iterator()
    override fun listIterator(): ListIterator<FFIPointer?> = vlist.listIterator()
    override fun listIterator(index: Int): ListIterator<FFIPointer?> = vlist.listIterator(index)
    override fun subList(fromIndex: Int, toIndex: Int): List<FFIPointer?> = TODO()
    override fun indexOf(element: FFIPointer?): Int {
        for (n in 0 until size) if (this[n] == element) return n
        return -1
    }
    override fun lastIndexOf(element: FFIPointer?): Int {
        for (n in size - 1 downTo 0) if (this[n] == element) return n
        return -1
    }
    override fun containsAll(elements: Collection<FFIPointer?>): Boolean = vlist.containsAll(elements)
    override fun contains(element: FFIPointer?): Boolean = indexOf(element) >= 0
}

fun FFIPointer.withOffset(offset: Int): FFIPointer? = CreateFFIPointer(address + offset)

fun Buffer.getFFIPointer(offset: Int): FFIPointer? {
    return CreateFFIPointer(if (FFI_POINTER_SIZE == 8) getInt64(offset) else getInt32(offset).toLong())
}
fun Buffer.setFFIPointer(offset: Int, value: FFIPointer?) {
    if (FFI_POINTER_SIZE == 8) setInt64(offset, value.address) else setInt32(offset, value.address.toInt())
}

fun Buffer.getUnalignedFFIPointer(offset: Int): FFIPointer? {
    return CreateFFIPointer(if (FFI_POINTER_SIZE == 8) getUnalignedInt64(offset) else getUnalignedInt32(offset).toLong())
}
fun Buffer.setUnalignedFFIPointer(offset: Int, value: FFIPointer?) {
    if (FFI_POINTER_SIZE == 8) setUnalignedInt64(offset, value.address) else setUnalignedInt32(offset, value.address.toInt())
}

expect fun FFILibSym(lib: FFILib): FFILibSym

interface FFILibSym : Closeable {
    fun <T> get(name: String, type: KType): T = TODO()
    override fun close() {}
}

class FuncType(val params: List<KType?>, val ret: KType?) {
    val paramsClass = params.map { it?.classifier }
    val retClass = ret?.classifier
}

open class FFILib(val paths: List<String>, val lazyCreate: Boolean = true) {
    @OptIn(SyncIOAPI::class)
    val resolvedPath by lazy { LibraryResolver.resolve(*paths.toTypedArray()) }

    constructor(vararg paths: String?, lazyCreate: Boolean = true) : this(paths.toList().filterNotNull(), lazyCreate = lazyCreate)

    val functions = arrayListOf<FuncDelegate<*>>()
    open protected fun createFFILibSym(): FFILibSym =  FFILibSym(this)
    var _sym: FFILibSym? = null
    val sym: FFILibSym get() {
        if (_sym == null) _sym = createFFILibSym()
        return _sym!!
    }
    //val loaded: Boolean get() = sym != null
    val loaded: Boolean get() = true

    companion object {
        fun extractTypeFunc(type: KType): FuncType {
            val generics = type.arguments.map { it.type }
            val ret = generics.last()
            val params = generics.dropLast(1)
            return FuncType(params, ret)
        }

    }

    class FuncDelegate<T>(val base: FFILib, val name: String, val type: KType) : ReadOnlyProperty<FFILib, T> {
        val parts = extractTypeFunc(type)
        //val generics = type.arguments.map { it.type?.classifier }
        val params = parts.paramsClass
        val ret = parts.retClass
        var cached: T? = null
        override fun getValue(thisRef: FFILib, property: KProperty<*>): T {
            if (cached == null) cached = base.sym.get(name, type)
            return cached.fastCastTo()
        }
    }

    class FuncInfo<T>(val type: KType, val extraName: String?) {
        operator fun provideDelegate(
            thisRef: FFILib,
            prop: KProperty<*>
        ): ReadOnlyProperty<FFILib, T> = FuncDelegate<T>(thisRef, extraName ?: prop.name, type).also {
            thisRef.functions.add(it)
            if (!thisRef.lazyCreate) it.getValue(thisRef, prop)
        }
    }

    inline fun <reified T : Function<*>> func(name: String? = null): FuncInfo<T> = FuncInfo<T>(typeOf<T>(), name)

    //inline fun <reified T : Function<*>> castToFunc(ptr: FFIPointer): T = sym.castToFunc(ptr, FuncInfo(typeOf<T>(), null))
    protected fun finalize() {
        sym
    }
}

open class SymbolResolverFFILib(val resolve: (String) -> FFIPointer?) : FFILib(lazyCreate = false) {
    override fun createFFILibSym(): FFILibSym {
        return object : FFILibSym {
            override fun <T> get(name: String, type: KType): T = resolve(name)?.castToFunc(type) ?: error("Can't find symbol '$name'")
        }
    }
}

/*
object LibC : FFILib("/System/Library/Frameworks/CoreFoundation.framework/CoreFoundation") {
    val cos by func<(value: Double) -> Double>()
    val cosf by func<(value: Float) -> Float>()
    init {
        finalize()
    }
}
 */

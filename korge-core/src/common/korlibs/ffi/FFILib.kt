package korlibs.ffi

import korlibs.datastructure.fastCastTo
import korlibs.io.file.sync.SyncIOAPI
import korlibs.io.lang.Closeable
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.*

expect class FFIPointer {
}
expect val FFIPointer?.str: String
expect fun FFIPointer.getStringz(): String
expect fun FFIPointer.readInts(size: Int, offset: Int = 0): IntArray

expect fun FFILibSym(lib: BaseLib): FFILibSym

interface FFILibSym : Closeable {
    fun readBytes(pos: Int, size: Int): ByteArray = TODO()
    fun writeBytes(pos: Int, data: ByteArray): Unit = TODO()
    fun allocBytes(bytes: ByteArray): Int = TODO()
    fun freeBytes(vararg ptrs: Int): Unit = TODO()
    fun <T> get(name: String): T = TODO()
    override fun close() {}
    fun <T> castToFunc(ptr: FFIPointer?, funcInfo: BaseLib.FuncInfo<T>): T = TODO()
}

abstract class BaseLib {
    val functions = arrayListOf<FuncDelegate<*>>()
    var loaded = false
    lateinit var sym: FFILibSym

    companion object {
        fun extractTypeFunc(type: KType): Pair<List<KClassifier?>, KClassifier?> {
            val generics = type.arguments.map { it.type?.classifier }
            val ret = generics.last()
            val params = generics.dropLast(1)
            return params to ret
        }

    }

    class FuncDelegate<T>(val base: BaseLib, val name: String, val type: KType) : ReadOnlyProperty<BaseLib, T> {
        val parts = extractTypeFunc(type)
        //val generics = type.arguments.map { it.type?.classifier }
        val params = parts.first
        val ret = parts.second
        var cached: T? = null
        override fun getValue(thisRef: BaseLib, property: KProperty<*>): T {
            if (cached == null) cached = base.sym.get(name)
            return cached.fastCastTo()
        }
    }

    class FuncInfo<T>(val type: KType, val extraName: String?) {
        operator fun provideDelegate(
            thisRef: BaseLib,
            prop: KProperty<*>
        ): ReadOnlyProperty<BaseLib, T> = FuncDelegate<T>(thisRef, extraName ?: prop.name, type).also { thisRef.functions.add(it) }
    }

    inline fun <reified T : Function<*>> func(name: String? = null): FuncInfo<T> = FuncInfo<T>(typeOf<T>(), name)

    inline fun <reified T : Function<*>> castToFunc(ptr: FFIPointer): T = sym.castToFunc(ptr, FuncInfo(typeOf<T>(), null))

    fun finalize() {
        sym = FFILibSym(this)
        loaded = true
    }
}

open class WASMLib(val content: ByteArray) : BaseLib() {
    //val memory: Buffer by lazy { sym.memory }

    val malloc: (Int) -> Int by func()
    val free: (Int) -> Unit by func()

    fun readBytes(pos: Int, size: Int): ByteArray = sym.readBytes(pos, size)
    fun writeBytes(pos: Int, data: ByteArray) = sym.writeBytes(pos, data)

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
}

//open class WASMLib private constructor(content: Any?, dummy: Unit) : FFILib(listOf(), content, FFILibKind.WASM){
//    constructor(content: ByteArray) : this(content, Unit)
//    constructor(content: VfsFile) : this(content, Unit)
//}

open class FFILib(val paths: List<String>) : BaseLib() {
    @OptIn(SyncIOAPI::class)
    val resolvedPath by lazy { LibraryResolver.resolve(*paths.toTypedArray()) }

    constructor(vararg paths: String?) : this(paths.toList().filterNotNull())
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
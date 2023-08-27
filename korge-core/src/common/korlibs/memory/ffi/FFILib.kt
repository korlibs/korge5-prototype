package korlibs.memory.ffi

import korlibs.datastructure.fastCastTo
import korlibs.memory.Buffer
import korlibs.memory.setArrayInt8
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

expect class FFIPointer {
}
expect val FFIPointer?.str: String
expect fun FFIPointer.getStringz(): String
expect fun FFIPointer.readInts(size: Int, offset: Int = 0): IntArray

expect class FFILibSym(lib: FFILib) {
    val memory: Buffer
    fun <T> get(name: String): T
}

enum class FFILibKind { NATIVE, WASM }

open class WASMLib(content: ByteArray) : FFILib(listOf(), content, FFILibKind.WASM) {
    val memory: Buffer by lazy { sym.memory }

    val malloc: (Int) -> Int by func()
    val free: (Int) -> Unit by func()

    fun allocBytes(bytes: ByteArray): Int {
        val ptr = malloc(bytes.size)
        memory.setArrayInt8(ptr, bytes)
        return ptr
    }
}

//open class WASMLib private constructor(content: Any?, dummy: Unit) : FFILib(listOf(), content, FFILibKind.WASM){
//    constructor(content: ByteArray) : this(content, Unit)
//    constructor(content: VfsFile) : this(content, Unit)
//}

open class FFILib(val paths: List<String>, val content: ByteArray? = null, val kind: FFILibKind = FFILibKind.NATIVE) {
    constructor(vararg paths: String?, kind: FFILibKind = FFILibKind.NATIVE) : this(paths.toList().filterNotNull(), kind = kind)
    constructor(data: ByteArray) : this(emptyList(), data, FFILibKind.WASM)
    //constructor(data: VfsFile) : this(emptyList(), data, FFILibKind.WASM)

    val functions = arrayListOf<FuncDelegate<*>>()
    var loaded = false
    lateinit var sym: FFILibSym

    inner class FuncDelegate<T>(val name: String, val type: KType) : ReadOnlyProperty<FFILib, T> {
        val generics = type.arguments.map { it.type?.classifier }
        val ret = generics.last()
        val params = generics.dropLast(1)
        var cached: T? = null
        override fun getValue(thisRef: FFILib, property: KProperty<*>): T {
            if (cached == null) cached = sym.get(name)
            return cached.fastCastTo()
        }
    }

    inner class FuncInfo<T>(val type: KType, val extraName: String?) {
        operator fun provideDelegate(
            thisRef: FFILib,
            prop: KProperty<*>
        ): ReadOnlyProperty<FFILib, T> = FuncDelegate<T>(extraName ?: prop.name, type).also { functions.add(it) }
    }

    inline fun <reified T : Function<*>> func(name: String? = null): FuncInfo<T> = FuncInfo<T>(typeOf<T>(), name)

    fun finalize() {
        sym = FFILibSym(this)
        loaded = true
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
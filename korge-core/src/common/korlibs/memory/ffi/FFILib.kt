package korlibs.memory.ffi

import korlibs.datastructure.fastCastTo
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.*

expect class FFIPointer

expect class FFILibSym(lib: FFILib) {
    fun <T> get(name: String): T
}

open class FFILib(val paths: List<String>) {
    constructor(vararg paths: String?) : this(paths.toList().filterNotNull())
    val functions = arrayListOf<FuncDelegate<*>>()
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

    inner class FuncInfo<T>(val type: KType) {
        operator fun provideDelegate(
            thisRef: FFILib,
            prop: KProperty<*>
        ): ReadOnlyProperty<FFILib, T> = FuncDelegate<T>(prop.name, type).also { functions.add(it) }
    }

    inline fun <reified T : Function<*>> func(): FuncInfo<T> = FuncInfo<T>(typeOf<T>())

    fun finalize() {
        sym = FFILibSym(this)
    }
}

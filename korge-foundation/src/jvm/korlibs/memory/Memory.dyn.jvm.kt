@file:Suppress("PackageDirectoryMismatch")

package korlibs.memory.dyn

import com.sun.jna.*
import java.lang.reflect.*
import kotlin.reflect.*

operator fun <R> KPointerTT<KFunctionTT<() -> R>>.invoke(): R = this.ref!!.func.invoke()
operator fun <P1, R> KPointerTT<KFunctionTT<(P1) -> R>>.invoke(p1: P1): R = this.ref!!.func.invoke(p1)
operator fun <P1, P2, R> KPointerTT<KFunctionTT<(P1, P2) -> R>>.invoke(p1: P1, p2: P2): R = this.ref!!.func.invoke(p1, p2)
operator fun <P1, P2, P3, R> KPointerTT<KFunctionTT<(P1, P2, P3) -> R>>.invoke(p1: P1, p2: P2, p3: P3): R = this.ref!!.func.invoke(p1, p2, p3)
operator fun <P1, P2, P3, P4, R> KPointerTT<KFunctionTT<(P1, P2, P3, P4) -> R>>.invoke(p1: P1, p2: P2, p3: P3, p4: P4): R = this.ref!!.func.invoke(p1, p2, p3, p4)
operator fun <P1, P2, P3, P4, P5, R> KPointerTT<KFunctionTT<(P1, P2, P3, P4, P5) -> R>>.invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5): R = this.ref!!.func.invoke(p1, p2, p3, p4, p5)
operator fun <P1, P2, P3, P4, P5, P6, R> KPointerTT<KFunctionTT<(P1, P2, P3, P4, P5, P6) -> R>>.invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6): R = this.ref!!.func.invoke(p1, p2, p3, p4, p5, p6)
operator fun <P1, P2, P3, P4, P5, P6, P7, R> KPointerTT<KFunctionTT<(P1, P2, P3, P4, P5, P6, P7) -> R>>.invoke(p1: P1, p2: P2, p3: P3, p4: P4, p5: P5, p6: P6, p7: P7): R = this.ref!!.func.invoke(p1, p2, p3, p4, p5, p6, p7)

public actual open class DynamicLibraryBase actual constructor(names: List<String>) : DynamicSymbolResolver {
    val library: NativeLibrary? = run {
        var ex: Throwable? = null
        for (name in names) {
            try {
                val instance = NativeLibrary.getInstance(name)
                if (instance != null) return@run instance
            } catch (e: Throwable) {
                if (ex == null) ex = e
            }
        }
        if (ex != null) {
            ex?.printStackTrace()
        }
        null
    }

    actual val isAvailable: Boolean get() = false
    override fun getSymbol(name: String): KPointer? {
        return KPointerTT(library?.getGlobalVariableAddress(name))
    }
    actual fun close() {
        library?.close()
    }
}

actual inline fun <reified T : Function<*>> DynamicLibrary.func(name: String?): DynamicFunLibraryNotNull<T> = DynamicFun<T>(this, name, T::class, typeOf<T>())
//public fun <T : Function<*>> DynamicLibrary.funcNull(name: String? = null): DynamicFunLibraryNull<T> = DynamicFunLibraryNull<T>(this, name)

//actual inline fun <reified T : Function<*>> DynamicLibrary.sfunc(name: String? = null): DynamicFunLibrary<T>
//actual inline fun <reified T : Function<*>> DynamicLibrary.sfuncNull(name: String? = null): DynamicFunLibraryNull<T>


@OptIn(ExperimentalStdlibApi::class)
public open class DynamicFun<T : Function<*>>(
    library: DynamicSymbolResolver,
    name: String? = null,
    val clazz: KClass<T>,
    val funcType: KType
) : DynamicFunLibraryNotNull<T>(library, name) {
    fun Type.getFinalClass(): Class<*> {
        return when (this) {
            is Class<*> -> this
            is ParameterizedType -> this.rawType.getFinalClass()
            else -> TODO("$this")
        }
    }

    override fun getValue(obj: Any?, property: KProperty<*>): KPointerTT<KFunctionTT<T>> {
        val rname = name ?: property.name
        val symbol: KPointer = library.getSymbol(rname) ?: error("Can't find symbol '$rname'")
        val func = com.sun.jna.Function.getFunction(symbol.ptr)
        val jtype = funcType.arguments.last().type!!.javaType
        val retType: Class<*> = jtype.getFinalClass()
        val classLoader = this::class.java.classLoader
        val interfaces = arrayOf(clazz.java)

        return KPointerTT<KFunctionTT<T>>(Pointer.createConstant(0), KFunctionTT(when {
            retType.isAssignableFrom(Void::class.java) -> Proxy.newProxyInstance(classLoader, interfaces) { _, _, args -> func.invokeVoid(convertArgs(args)) }
            retType.isAssignableFrom(Unit::class.java) -> Proxy.newProxyInstance(classLoader, interfaces) { _, _, args -> func.invokeVoid(convertArgs(args)) }
            retType.isAssignableFrom(Double::class.java) -> Proxy.newProxyInstance(classLoader, interfaces) { _, _, args -> func.invokeDouble(convertArgs(args)) }
            retType.isAssignableFrom(Float::class.java) -> Proxy.newProxyInstance(classLoader, interfaces) { _, _, args -> func.invokeFloat(convertArgs(args)) }
            retType.isAssignableFrom(Int::class.java) -> Proxy.newProxyInstance(classLoader, interfaces) { _, _, args -> func.invokeInt(convertArgs(args)) }
            retType.isAssignableFrom(Pointer::class.java) -> Proxy.newProxyInstance(classLoader, interfaces) { _, _, args -> func.invokePointer(convertArgs(args)) }
            retType.isAssignableFrom(KPointerTT::class.java) -> Proxy.newProxyInstance(classLoader, interfaces) { _, _, args -> KPointerTT<KPointed>(func.invokePointer(convertArgs(args))) }
            else -> Proxy.newProxyInstance(classLoader, interfaces) { _, _, args -> func.invokeDouble(args) }
        } as T))
    }

    fun convertArgs(args: Array<Any?>): Array<Any?> {
        val out = args.copyOf()
        for (n in 0 until out.size) {
            val it = out[n]
            out[n] = when (it) {
                is KPointerTT<*> -> it.ptr
                else -> it
            }
        }
        return out
    }
}

fun Memory(data: ByteArray): Memory {
    val out = Memory(data.size.toLong())
    for (n in data.indices) out.setByte((n).toLong(), data[n])
    return out
}

fun Memory(data: IntArray): Memory {
    val out = Memory(data.size.toLong() * 4)
    for (n in data.indices) out.setInt((n * 4).toLong(), data[n])
    return out
}

fun Memory(data: LongArray): Memory {
    val out = Memory(data.size.toLong() * 8)
    for (n in data.indices) out.setLong((n * 8).toLong(), data[n])
    return out
}

actual class KArena actual constructor() {
    private val pointers = arrayListOf<Memory>()
    actual fun allocBytes(size: Int): KPointer = KPointer(Memory(size.toLong()).also {
        it.clear()
        pointers += it
    })
    actual fun clear() {
        for (n in 0 until pointers.size) pointers[n].clear()
        pointers.clear()
    }
}

actual val POINTER_SIZE: Int = 8
actual val LONG_SIZE: Int = 8

actual abstract class KPointed
actual class KPointerTT<T : KPointed>(val optr: Pointer?, val ref: T? = null) {
    val ptr: Pointer get() = optr!!
}
fun <T : KPointed> KPointer(ptr: Pointer): KPointerTT<T> = KPointerTT<T>(ptr, null)
val Pointer.kpointer: KPointer get() = KPointer(this)
actual class KFunctionTT<T : Function<*>>(val func: T) : KPointed()
//actual typealias NativeLong = com.sun.jna.NativeLong

//actual typealias KPointer = Pointer
actual abstract class KStructureBase : NativeMapped {
    actual abstract val pointer: KPointer?
    override fun nativeType(): Class<*> = Pointer::class.java
    override fun toNative(): Any? = this.pointer
    override fun fromNative(nativeValue: Any?, context: FromNativeContext?): Any = this::class.constructors.first().call(nativeValue)
}
actual fun KPointer(address: Long): KPointer = KPointer(Pointer(address))
actual val KPointer.address: Long get() = Pointer.nativeValue(this.ptr)

val Pointer.address: Long get() = Pointer.nativeValue(this)

actual fun KPointer.getByte(offset: Int): Byte = this.ptr.getByte(offset.toLong())
actual fun KPointer.setByte(offset: Int, value: Byte): Unit = this.ptr.setByte(offset.toLong(), value)
actual fun KPointer.getShort(offset: Int): Short = this.ptr.getShort(offset.toLong())
actual fun KPointer.setShort(offset: Int, value: Short): Unit = this.ptr.setShort(offset.toLong(), value)
actual fun KPointer.getInt(offset: Int): Int = this.ptr.getInt(offset.toLong())
actual fun KPointer.setInt(offset: Int, value: Int): Unit = this.ptr.setInt(offset.toLong(), value)
actual fun KPointer.getFloat(offset: Int): Float = this.ptr.getFloat(offset.toLong())
actual fun KPointer.setFloat(offset: Int, value: Float): Unit = this.ptr.setFloat(offset.toLong(), value)
actual fun KPointer.getDouble(offset: Int): Double = this.ptr.getDouble(offset.toLong())
actual fun KPointer.setDouble(offset: Int, value: Double): Unit = this.ptr.setDouble(offset.toLong(), value)
actual fun KPointer.getLong(offset: Int): Long = this.ptr.getLong(offset.toLong())
actual fun KPointer.setLong(offset: Int, value: Long): Unit = this.ptr.setLong(offset.toLong(), value)

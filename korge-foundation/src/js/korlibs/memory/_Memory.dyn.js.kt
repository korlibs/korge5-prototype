@file:Suppress("PackageDirectoryMismatch")

package korlibs.memory.dyn

import korlibs.memory.*
import kotlin.reflect.*

public actual open class DynamicLibraryBase actual constructor(names: List<String>) : DynamicSymbolResolver {
    actual val isAvailable: Boolean get() = false
    override fun getSymbol(name: String): KPointer? = TODO()
    actual fun close() {
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
    override fun getValue(obj: Any?, property: KProperty<*>): KPointerTT<KFunctionTT<T>> {
        // @TODO: We can call the global scope maybe?
        //return { } as T
        return KPointerTT(byteArrayOf())
    }
}

//actual inline operator fun <R> KPointerTT<KFunctionTT<() -> R>>.invoke(): R = TODO()
//actual inline operator fun <P1, R> KPointerTT<KFunctionTT<(P1) -> R>>.invoke(p1: P1): R = TODO()

actual class KArena actual constructor() {
    actual fun allocBytes(size: Int): KPointer = KPointer(ByteArray(size))
    actual fun clear(): Unit = Unit
}

actual val POINTER_SIZE: Int = 4
actual val LONG_SIZE: Int = 8

actual abstract class KPointed
actual class KPointerTT<T : KPointed>(val ptr: ByteArray)
fun <T : KPointed> KPointer(ptr: ByteArray): KPointerTT<T> = KPointerTT<T>(ptr)
actual class KFunctionTT<T : Function<*>>(val func: T) : KPointed()

actual abstract class KStructureBase {
    actual abstract val pointer: KPointer?
}
actual fun KPointer(address: Long): KPointer = TODO()
actual val KPointer.address: Long get() = TODO()

actual fun KPointer.getByte(offset: Int): Byte = this.ptr.readS8(offset).toByte()
actual fun KPointer.setByte(offset: Int, value: Byte): Unit = this.ptr.write8(offset, value.toInt())
actual fun KPointer.getShort(offset: Int): Short = this.ptr.readS16LE(offset).toShort()
actual fun KPointer.setShort(offset: Int, value: Short): Unit = this.ptr.write16LE(offset, value.toInt())
actual fun KPointer.getInt(offset: Int): Int = this.ptr.readS32LE(offset)
actual fun KPointer.setInt(offset: Int, value: Int): Unit = this.ptr.write32LE(offset, value)
actual fun KPointer.getFloat(offset: Int): Float = this.ptr.readF32LE(offset)
actual fun KPointer.setFloat(offset: Int, value: Float): Unit = this.ptr.writeF32LE(offset, value)
actual fun KPointer.getDouble(offset: Int): Double = this.ptr.readF64LE(offset)
actual fun KPointer.setDouble(offset: Int, value: Double): Unit = this.ptr.writeF64LE(offset, value)
actual fun KPointer.getLong(offset: Int): Long = this.ptr.readS64LE(offset)
actual fun KPointer.setLong(offset: Int, value: Long): Unit = this.ptr.write64LE(offset, value)

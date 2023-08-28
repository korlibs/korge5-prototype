package korlibs.ffi

import kotlin.reflect.KType

actual fun FFILibSym(lib: BaseLib): FFILibSym {
    return object : FFILibSym {
    }
}

actual class FFIPointer

actual fun CreateFFIPointer(ptr: Long): FFIPointer? = TODO()

actual val FFI_POINTER_SIZE: Int get() = TODO()

actual val FFIPointer?.address: Long get() = TODO()

actual fun FFIPointer.getStringz(): String {
    TODO("Not yet implemented")
}

actual val FFIPointer?.str: String
    get() = TODO("Not yet implemented")


actual fun FFIPointer.getIntArray(size: Int, offset: Int): IntArray {
    TODO("Not yet implemented")
}

actual fun <T> FFIPointer.castToFunc(type: KType): T {
    TODO("Not yet implemented")
}

actual fun FFIPointer.getUnalignedI8(offset: Int): Byte = TODO()
actual fun FFIPointer.getUnalignedI16(offset: Int): Short = TODO()
actual fun FFIPointer.getUnalignedI32(offset: Int): Int = TODO()
actual fun FFIPointer.getUnalignedI64(offset: Int): Long = TODO()
actual fun FFIPointer.getUnalignedF32(offset: Int): Float = TODO()
actual fun FFIPointer.getUnalignedF64(offset: Int): Double = TODO()

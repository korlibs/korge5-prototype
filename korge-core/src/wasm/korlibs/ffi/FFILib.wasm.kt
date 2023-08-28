package korlibs.ffi

import kotlin.reflect.KType

actual fun FFILibSym(lib: BaseLib): FFILibSym {
    return object : FFILibSym {
    }
}

actual class FFIPointer

actual fun CreateFFIPointer(ptr: Long): FFIPointer = TODO()

actual val FFIPointer?.address: Long get() = TODO()

actual fun FFIPointer.getStringz(): String {
    TODO("Not yet implemented")
}

actual val FFIPointer?.str: String
    get() = TODO("Not yet implemented")


actual fun FFIPointer.readInts(size: Int, offset: Int): IntArray {
    TODO("Not yet implemented")
}

actual fun <T> FFIPointer.castToFunc(type: KType): T {
    TODO("Not yet implemented")
}
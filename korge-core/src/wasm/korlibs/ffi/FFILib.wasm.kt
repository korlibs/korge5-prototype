package korlibs.ffi

actual fun FFILibSym(lib: BaseLib): FFILibSym {
    return object : FFILibSym {
    }
}

actual class FFIPointer

actual fun FFIPointer.getStringz(): String {
    TODO("Not yet implemented")
}

actual val FFIPointer?.str: String
    get() = TODO("Not yet implemented")


actual fun FFIPointer.readInts(size: Int, offset: Int): IntArray {
    TODO("Not yet implemented")
}
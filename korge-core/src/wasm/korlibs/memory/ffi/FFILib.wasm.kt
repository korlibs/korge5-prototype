package korlibs.memory.ffi

actual class FFILibSym actual constructor(lib: FFILib) {
    actual fun <T> get(name: String): T {
        TODO("Not yet implemented")
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
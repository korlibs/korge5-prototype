package korlibs.ffi

actual class FFILibSym actual constructor(lib: BaseLib) {
    actual fun <T> get(name: String): T {
        TODO("Not yet implemented")
    }

    actual fun readBytes(pos: Int, size: Int): ByteArray {
        TODO("Not yet implemented")
    }

    actual fun writeBytes(pos: Int, data: ByteArray) {
    }

    actual fun allocBytes(bytes: ByteArray): Int {
        TODO("Not yet implemented")
    }

    actual fun freeBytes(vararg ptrs: Int) {
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
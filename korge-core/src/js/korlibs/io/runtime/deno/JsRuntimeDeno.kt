package korlibs.io.runtime.deno

import korlibs.bignumber.BigInt
import korlibs.io.file.*
import korlibs.io.jsObject
import korlibs.io.jsObjectToMap
import korlibs.io.runtime.JsRuntime
import korlibs.io.runtime.node.asUint8Array
import korlibs.io.stream.AsyncStream
import korlibs.io.stream.AsyncStreamBase
import korlibs.io.stream.toAsyncStream
import korlibs.io.util.JsBigInt
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.khronos.webgl.*
import kotlin.js.Date
import kotlin.js.Promise


//@JsName("Deno")
//external class DenoPointer
class DenoPointer

val DenoPointer.value: JsBigInt get() = Deno.UnsafePointer.value(this)

fun DenoPointer.readStringz(offset: Int = 0): String {
    return Deno.UnsafePointerView.getCString(this, offset)
}

fun def(result: dynamic, vararg params: dynamic): dynamic =
    jsObject("parameters" to params, "result" to result)

/*
val denoBase = Deno.dlopen<dynamic>(
    "/System/Library/Frameworks/CoreFoundation.framework/CoreFoundation",
    jsObject(
        "memcpy" to def("pointer", "buffer", "pointer", "usize"),
        "strlen" to def("i32", "pointer"),
        "malloc" to def("pointer", "usize"),
        "free" to def("void", "pointer"),
    )
).symbols
val denoBase2 = Deno.dlopen<dynamic>(
    "/System/Library/Frameworks/CoreFoundation.framework/CoreFoundation",
    jsObject(
        "memcpy" to def("pointer", "pointer", "buffer", "usize"),
    )
).symbols

val denoBaseSize = Deno.dlopen<dynamic>(
    "/System/Library/Frameworks/CoreFoundation.framework/CoreFoundation",
    jsObject(
        "memcpy" to def("usize", "usize", "usize", "usize"),
        "strlen" to def("i32", "usize"),
        "malloc" to def("usize", "usize"),
        "free" to def("void", "usize"),
    )
).symbols

fun DenoPointer.view() {
    Deno.UnsafePointer.create()
}

fun Deno_allocBytes(bytes: ByteArray): DenoPointer {
    val ptr = denoBase.malloc(bytes.size)
    denoBase2.memcpy(ptr, bytes, bytes.size)
    return ptr
}

fun Deno_free(ptr: DenoPointer) {
    denoBase.free(ptr)
}

fun DenoPointer.readStringz(): String {
    val len = strlen()
    return readBytes(len).decodeToString()
}

fun DenoPointer.strlen(): Int = denoBase.strlen(this)

fun DenoPointer.readBytes(size: Int): ByteArray {
    val data = ByteArray(size)
    denoBase.memcpy(data, this, size)
    return data
}

fun DenoPointer.writeBytes(data: ByteArray) {
    denoBase2.memcpy(this, data, data.size)
}
*/

object JsRuntimeDeno : JsRuntime() {
    override fun existsSync(path: String): Boolean = try {
        Deno.statSync(path)
        true
    } catch (e: dynamic) {
        false
    }

    override fun currentDir(): String = Deno.cwd()

    override fun env(key: String): String? = Deno.env.get(key)
    override fun envs() = jsObjectToMap(Deno.env.toObject())

    override fun openVfs(path: String): VfsFile {
        return DenoLocalVfs()[if (path == ".") currentDir() else path]
    }
}

class DenoLocalVfs : Vfs() {
    private fun getFullPath(path: String): String {
        return path.pathInfo.normalize()
    }

    override suspend fun open(path: String, mode: VfsOpenMode): AsyncStream {
        val options = jsObject(
            "read" to mode.read,
            "write" to mode.write,
            "append" to mode.append,
            "truncate" to mode.truncate,
            //"create" to !mode.createIfNotExists,
            "create" to mode.write,
            "createNew" to mode.createIfNotExists,
            "mode" to "666".toInt(8)
        )
        val file = Deno.open(getFullPath(path), options).await()
        return DenoAsyncStreamBase(file).toAsyncStream()
    }

    override suspend fun listFlow(path: String): Flow<VfsFile> =
        Deno.readDir(getFullPath(path)).toFlow().map { VfsFile(this, "$path/${it.name}") }

    override suspend fun mkdir(path: String, attributes: List<Attribute>): Boolean = try {
        Deno.mkdir(getFullPath(path), jsObject("recursive" to true)).await()
        true
    } catch (e: Throwable) {
        false
    }

    override suspend fun delete(path: String): Boolean = try {
        Deno.remove(getFullPath(path), jsObject("recursive" to false)).await()
        true
    } catch (e: Throwable) {
        false
    }

    override suspend fun rename(src: String, dst: String): Boolean = try {
        Deno.rename(getFullPath(src), getFullPath(dst)).await()
        true
    } catch (e: Throwable) {
        false
    }

    override suspend fun stat(path: String): VfsStat {
        return try {
            Deno.stat(getFullPath(path)).await().let {
                createExistsStat(
                    path, it.isDirectory, it.size.toLong(), it.dev.toLong(),
                    it.ino?.toLong() ?: -1L, it.mode?.toInt() ?: "777".toInt(8)
                )
            }
        } catch (e: Throwable) {
            createNonExistsStat(path)
        }
    }
}

class DenoAsyncStreamBase(val file: DenoFsFile) : AsyncStreamBase() {
    override suspend fun read(position: Long, buffer: ByteArray, offset: Int, len: Int): Int {
        file.seek(position.toDouble()).await()
        val read = file.read(Uint8Array(buffer.asUint8Array().buffer, offset, len)).await()
        return read.toInt()
    }

    override suspend fun write(position: Long, buffer: ByteArray, offset: Int, len: Int) {
        file.seek(position.toDouble()).await()
        file.write(Uint8Array(buffer.asUint8Array().buffer, offset, len)).await()
    }

    override suspend fun setLength(value: Long) {
        file.truncate(value.toDouble()).await()
    }

    override suspend fun getLength(): Long {
        return file.stat().await().size.toLong()
    }

    override suspend fun close() {
        file.close()
    }
}


external interface DenoFileInfo {
    val isFile: Boolean
    val isDirectory: Boolean
    val isSymlink: Boolean
    val size: Double
    val mtime: Date?
    val atime: Date?
    val birthtime: Date?
    val dev: Double
    val ino: Double?
    val mode: Double?
    val nlink: Double?
    val uid: Double?
    val gid: Double?
    val rdev: Double?
    val blksize: Double?
    val blocks: Double?
    val isBlockDEvice: Double?
    val isFifo: Double?
    val isSocket: Double?
}

external interface DenoFsFile {
    fun truncate(len: Double): Promise<dynamic>
    fun close()
    fun seek(pos: Double): Promise<Double>
    fun write(data: Uint8Array): Promise<dynamic>
    fun read(data: Uint8Array): Promise<Double>
    fun stat(): Promise<DenoFileInfo>
}

external interface DenoDlOpen<T> {
    val symbols: T
}

external interface DenoBuild {
    /** aarch64-apple-darwin */
    val target: String
    /** aarch64 */
    val arch: String
    /** darwin, linux, windows */
    val os: String
    /** apple */
    val vendor: String
    //{
    //    target: "aarch64-apple-darwin",
    //    arch: "aarch64",
    //    os: "darwin",
    //    vendor: "apple",
    //    env: undefined
    //}
}

external object Deno {
    fun exit(exitCode: Int = definedExternally)
    fun statSync(path: String): dynamic
    fun cwd(): String
    fun open(path: String, options: dynamic): Promise<DenoFsFile>
    fun readDir(path: String): JSAsyncIterable<DenoDirEntry>
    fun mkdir(path: String, options: dynamic): Promise<Unit>
    fun remove(path: String, options: dynamic): Promise<Unit>
    fun rename(oldPath: String, newPath: String): Promise<Unit>
    fun stat(path: String): Promise<DenoFileInfo>
    fun <T> dlopen(path: String, symbols: dynamic): DenoDlOpen<T>
    fun addSignalListener(s: String, function: () -> Unit)

    val build: DenoBuild

    val env: dynamic

    object UnsafePointer {
        fun create(value: JsBigInt): DenoPointer
        fun equals(a: JsBigInt, b: JsBigInt): Boolean
        fun of(a: ArrayBufferView): DenoPointer
        fun offset(a: ArrayBufferView, offset: Int): DenoPointer
        fun value(value: DenoPointer): JsBigInt
    }

    class UnsafePointerView {
        constructor(pointer: DenoPointer)
        val pointer: DenoPointer
        fun getBigInt64(): BigInt
        fun getBigInt64(offset: Int): BigInt
        companion object {
            fun getCString(pointer: DenoPointer, offset: Int): String
        }
    }
}

external interface DenoDirEntry {
    val name: String
    val isFile: Boolean
    val isDirectory: Boolean
    val isSymlink: Boolean
}

external interface JSAsyncIterable<T>

val Symbol_asyncIterator get() = Symbol.asyncIterator

external val Symbol: dynamic

external interface JSIterableResult<T> {
    val value: T
    val done: Boolean
}

suspend fun <T> JSAsyncIterable<T>.toFlow(): Flow<T> = flow {
    val iterator = (this@toFlow.asDynamic())[Symbol_asyncIterator]
    val gen = iterator.call(this)
    //println(gen)
    while (true) {
        val prom = gen.next().unsafeCast<Promise<JSIterableResult<T>>>()
        val value = prom.await()
        if (value.done) break
        emit(value.value)
    }
}
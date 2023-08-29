package korlibs.ffi

import com.sun.jna.Function
import com.sun.jna.Memory
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer
import korlibs.datastructure.lock.Lock
import korlibs.io.file.sync.*
import korlibs.io.serialization.json.Json
import korlibs.memory.*
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.Proxy
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.typeOf

actual fun FFILibSym(lib: BaseLib): FFILibSym {
    return FFILibSymJVM(lib)
}

actual typealias FFIPointer = Pointer
actual typealias FFIMemory = Memory

actual fun CreateFFIMemory(size: Int): FFIMemory = Memory(size.toLong())
actual fun CreateFFIMemory(bytes: ByteArray): FFIMemory = Memory(bytes.size.toLong()).also { it.write(0L, bytes, 0, bytes.size) }
actual val FFIMemory.pointer: FFIPointer get() = this

@JvmName("FFIPointerCreation")
actual fun CreateFFIPointer(ptr: Long): FFIPointer? = if (ptr == 0L) null else Pointer(ptr)
actual val FFI_POINTER_SIZE: Int = 8

actual fun FFIPointer.getStringz(): String = this.getString(0L)
actual val FFIPointer?.address: Long get() = Pointer.nativeValue(this)
actual val FFIPointer?.str: String get() = this.toString()
actual fun FFIPointer.getIntArray(size: Int, offset: Int): IntArray = this.getIntArray(0L, size)

actual fun FFIPointer.getUnalignedI8(offset: Int): Byte = this.getByte(offset.toLong())
actual fun FFIPointer.getUnalignedI16(offset: Int): Short = this.getShort(offset.toLong())
actual fun FFIPointer.getUnalignedI32(offset: Int): Int = this.getInt(offset.toLong())
actual fun FFIPointer.getUnalignedI64(offset: Int): Long = this.getLong(offset.toLong())
actual fun FFIPointer.getUnalignedF32(offset: Int): Float = this.getFloat(offset.toLong())
actual fun FFIPointer.getUnalignedF64(offset: Int): Double = this.getDouble(offset.toLong())

actual fun FFIPointer.setUnalignedI8(value: Byte, offset: Int) = this.setByte(offset.toLong(), value)
actual fun FFIPointer.setUnalignedI16(value: Short, offset: Int) = this.setShort(offset.toLong(), value)
actual fun FFIPointer.setUnalignedI32(value: Int, offset: Int) = this.setInt(offset.toLong(), value)
actual fun FFIPointer.setUnalignedI64(value: Long, offset: Int) = this.setLong(offset.toLong(), value)
actual fun FFIPointer.setUnalignedF32(value: Float, offset: Int) = this.setFloat(offset.toLong(), value)
actual fun FFIPointer.setUnalignedF64(value: Double, offset: Int) = this.setDouble(offset.toLong(), value)

actual fun <T> FFIPointer.castToFunc(type: KType): T =
    createJNAFunctionToPlainFunc(Function.getFunction(this), type)

fun <T : kotlin.Function<*>> createJNAFunctionToPlainFunc(func: Function, type: KType): T {
    val (params, ret) = BaseLib.extractTypeFunc(type)

    return Proxy.newProxyInstance(
        FFILibSymJVM::class.java.classLoader,
        arrayOf((type.classifier as KClass<*>).java)
    ) { proxy, method, args ->
        val targs = (args ?: emptyArray()).map {
            when (it) {
                is FFIPointerArray -> it.data
                is Buffer -> it.buffer
                else -> it
            }
        }.toTypedArray()
        when (ret) {
            Unit::class -> func.invokeVoid(targs)
            Int::class -> func.invokeInt(targs)
            Float::class -> func.invokeFloat(targs)
            Double::class -> func.invokeDouble(targs)
            else -> func.invoke((ret as KClass<*>).java, targs)
        }
    } as T
}

fun <T : kotlin.Function<*>> createWasmFunctionToPlainFunction(wasm: DenoWASM, funcName: String, type: KType): T {
    val (params, ret) = BaseLib.extractTypeFunc(type)
    return Proxy.newProxyInstance(
        FFILibSymJVM::class.java.classLoader,
        arrayOf((type.classifier as KClass<*>).java)
    ) { proxy, method, args ->
        val sargs = args ?: emptyArray()
        //return wasm.evalWASMFunction(nfunc.name, *sargs)
        wasm.executeFunction(funcName, *sargs)
    } as T
}

fun <T : kotlin.Function<*>> createWasmFunctionToPlainFunctionIndirect(wasm: DenoWASM, address: Int, type: KType): T {
    val (params, ret) = BaseLib.extractTypeFunc(type)
    return Proxy.newProxyInstance(
        FFILibSymJVM::class.java.classLoader,
        arrayOf((type.classifier as KClass<*>).java)
    ) { proxy, method, args ->
        val sargs = args ?: emptyArray()
        //return wasm.evalWASMFunction(nfunc.name, *sargs)
        wasm.executeFunctionIndirect(address, *sargs)
    } as T
}

inline fun <reified T : kotlin.Function<*>> createJNAFunctionToPlainFunc(func: Function): T =
    createJNAFunctionToPlainFunc(func, typeOf<T>())

class FFILibSymJVM(val lib: BaseLib) : FFILibSym {
    @OptIn(SyncIOAPI::class)
    val nlib by lazy {
        lib as FFILib
        val resolvedPaths = listOf(LibraryResolver.resolve(*lib.paths.toTypedArray()))
        resolvedPaths.firstNotNullOfOrNull {
            NativeLibrary.getInstance(it)
        }
    }

    val libWasm = lib as? WASMLib?

    fun <T : kotlin.Function<*>> createFunction(funcName: String, type: KType): T {
        return if (libWasm != null) {
            createWasmFunctionToPlainFunction<T>(wasm, funcName, type)
        } else {
            val func: Function = nlib!!.getFunction(funcName) ?: error("Can't find function ${funcName}")
            createJNAFunctionToPlainFunc<T>(func, type)
        }
    }

    val functions: Map<String, kotlin.Function<*>> by lazy {
        lib.functions.associate { nfunc ->
            //val lib = NativeLibrary.getInstance("")
            nfunc.name to createFunction(nfunc.name, nfunc.type)
        }
    }

    val wasm: DenoWASM by lazy {
        if (libWasm == null) error("Not a WASM module")
        DenoWasmProcessStdin.open(libWasm.content)
    }

    override fun <T> get(name: String, type: KType): T = functions[name] as T
    override fun readBytes(pos: Int, size: Int): ByteArray = wasm.readBytes(pos, size)
    override fun writeBytes(pos: Int, data: ByteArray) = wasm.writeBytes(pos, data)
    override fun allocBytes(bytes: ByteArray): Int = wasm.allocAndWrite(bytes)
    override fun freeBytes(vararg ptrs: Int) = wasm.free(*ptrs)
    override fun stackSave(): Int = wasm.stackSave()
    override fun stackRestore(ptr: Int) = wasm.stackRestore(ptr)
    override fun stackAlloc(size: Int): Int = wasm.stackAlloc(size)
    override fun stackAllocAndWrite(bytes: ByteArray): Int = wasm.stackAllocAndWrite(bytes)

    override fun <T> wasmFuncPointer(address: Int, type: KType): T =
        createWasmFunctionToPlainFunctionIndirect(wasm, address, type)

    override fun close() {
        if (libWasm != null) {
            wasm.close()
        }
    }
}

private val PATHS: List<String> by lazy { System.getenv("PATH").split(File.pathSeparatorChar) }

object DenoWasmProcessStdin {
    val temp = System.getProperty("java.io.tmpdir")
    val file = File("$temp/korge.wasm.js")
        .also { it.writeText(DenoWasmServerStdinCode) }

    val commands: List<String> by lazy {
        ExecutableResolver.findInPaths("deno", PATHS)?.let { return@lazy listOf(it, "run", "-A", "--unstable", file.absolutePath) }
        ExecutableResolver.findInPaths("node", PATHS)?.let { return@lazy listOf(it, file.absolutePath) }
        emptyList()
    }
    val process = ProcessBuilder(commands)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        //.also { println("WASM: $commands") }
        .start()

    val io = DenoWasmIO(process.outputStream, process.inputStream, Closeable { })
    var lastId = 1

    fun open(wasmModuleBytes: ByteArray): DenoWASM {
        return DenoWASM(io, lastId++, wasmModuleBytes)
    }

    fun close() {
        process.destroy()
    }
}

class DenoWASM(val io: DenoWasmIO, val streamId: Int, val wasmModuleBytes: ByteArray) : Closeable {
    init {
        io.loadWASM(streamId, wasmModuleBytes)
    }
    //fun open(bytes: ByteArray) { io.loadWASM(streamId, bytes) }

    fun allocAndWrite(bytes: ByteArray): Int = io.allocAndWrite(streamId, bytes)
    fun free(vararg ptrs: Int) { io.free(streamId, *ptrs) }

    fun stackSave(): Int = io.stackSave(streamId)
    fun stackRestore(ptr: Int) = io.stackRestore(streamId, ptr)
    fun stackAlloc(size: Int): Int = io.stackAlloc(streamId, size)
    fun stackAllocAndWrite(bytes: ByteArray): Int = io.stackAllocAndWrite(streamId, bytes)

    fun writeBytes(ptr: Int, bytes: ByteArray) { io.writeBytes(streamId, ptr, bytes) }
    fun readBytes(ptr: Int, len: Int): ByteArray = io.readBytes(streamId, ptr, len)

    fun executeFunction(name: String, vararg params: Any?): Any? = io.executeFunction(streamId, name, *params)
    fun executeFunctionIndirect(address: Int, vararg params: Any?): Any? = io.executeFunctionIndirect(streamId, address, *params)

    override fun close() { io.unloadWASM(streamId) }
}

class DenoWasmIO(
    val output: OutputStream,
    val input: InputStream,
    val close: Closeable? = null,
    val debug: Boolean = false,
) {

    fun close() {
        close?.close()
    }

    fun loadWASM(streamId: Int, bytes: ByteArray) {
        writeReadMessage(1000, streamId, bytes)
        if (debug) println("CMD:loadWASM")
    }

    fun unloadWASM(streamId: Int) {
        writeReadMessage(1001, streamId, byteArrayOf())
        if (debug) println("CMD:unloadWASM")
    }

    fun writeBytes(streamId: Int, ptr: Int, bytes: ByteArray) {
        val payload = ByteArray(4 + bytes.size)
        payload.write32LE(0, ptr)
        payload.writeBytes(4, bytes)
        writeReadMessage(1010, streamId, payload)
        if (debug) println("CMD:writeBytes:ptr=$ptr, bytes=${bytes.size}")
    }

    fun allocAndWrite(streamId: Int, bytes: ByteArray): Int {
        return writeReadMessage(1011, streamId, bytes).readS32LE(0)
    }
    fun free(streamId: Int, vararg ptrs: Int) {
        val payload = ByteArray(ptrs.size * 4)
        for (n in 0 until ptrs.size) {
            payload.write32LE(n * 4, ptrs[n])
        }
        writeReadMessage(1012, streamId, payload)
        if (debug) println("CMD:free:ptrs=${ptrs.toList()}")
    }

    fun stackSave(streamId: Int): Int {
        return writeReadMessage(1013, streamId, ByteArray(0)).readS32LE(0)
    }
    fun stackRestore(streamId: Int, ptr: Int) {
        val payload = ByteArray(4)
        payload.write32LE(0, ptr)
        writeReadMessage(1014, streamId, payload)
    }
    fun stackAlloc(streamId: Int, size: Int): Int {
        val payload = ByteArray(4)
        payload.write32LE(0, size)
        return writeReadMessage(1015, streamId, payload).readS32LE(0)
    }
    fun stackAllocAndWrite(streamId: Int, bytes: ByteArray): Int {
        return writeReadMessage(1016, streamId, bytes).readS32LE(0)
    }

    fun readBytes(streamId: Int, ptr: Int, len: Int): ByteArray {
        val payload = ByteArray(8)
        payload.write32LE(0, ptr)
        payload.write32LE(4, len)
        if (debug) println("CMD:readBytes:ptr=$ptr,len=$len")
        return writeReadMessage(1020, streamId, payload)
    }

    fun executeFunction(streamId: Int, name: String, vararg params: Any?): Any? {
        val resultBytes = writeReadMessage(1030, streamId, Json.stringify(mapOf("func" to name, "params" to params.toList())).encodeToByteArray())
        if (debug) println("CMD:executeFunction:name=$name, params=${params.toList()}")
        //return if (resultBytes.isEmpty()) null else Json.parse(resultBytes.decodeToString())
        return if (resultBytes.isEmpty()) Unit else Json.parse(resultBytes.decodeToString())
    }

    fun executeFunctionIndirect(streamId: Int, address: Int, vararg params: Any?): Any? {
        val resultBytes = writeReadMessage(1030, streamId, Json.stringify(mapOf("indirect" to address, "params" to params.toList())).encodeToByteArray())
        val data = resultBytes.decodeToString()
        if (debug) println("CMD:executeFunctionIndirect:address=$address, params=${params.toList()}")
        return Json.parse(data)
    }

    private val lock = Lock()

    private fun writeReadMessage(type: Int, streamId: Int, payload: ByteArray): ByteArray = lock {
        _writeMessage(type, streamId, payload)
        return _readMessage(type)
    }

    private fun _writeMessage(type: Int, streamId: Int, payload: ByteArray) {
        val buffer = ByteArray(12)
        buffer.write32LE(0, type)
        buffer.write32LE(4, streamId)
        buffer.write32LE(8, payload.size)
        //println("writeMessage: type=$type, payload=${payload.size}")
        //socket.channel.write(arrayOf(buffer, ByteBuffer.wrap(payload)))
        output.write(buffer)
        output.write(payload)
        output.flush()
    }

    private fun _readMessage(type: Int): ByteArray {
        //println(" -- readMessage type=$type")
        val sizeBuffer = input.readBytesExact(4)
        val len = sizeBuffer.readS32LE(0)
        //println(" -- readMessage: len=$len")
        val out = input.readBytesExact(len)
        //println(" --> ${out.size}")
        return out
    }

    private fun InputStream.readBytesExact(size: Int): ByteArray {
        return readBytesExact(ByteArray(size))
    }

    private fun InputStream.readBytesExact(b: ByteArray): ByteArray {
        var offset = 0
        var remaining = b.size
        //println("readBytesExact[$remaining]")
        while (remaining > 0) {
            //println("   - READ $offset, $remaining")
            val len = read(b, offset, remaining)
            //println("      -> $len")
            if (len < 0) {
                error("InputStream error: len=$len")
            }
            if (len <= 0) break
            offset += len
            remaining -= len
        }
        check(remaining == 0) { "remaining == 0 : $remaining" }
        //println("REMAINING: $remaining : ${b.toList()}")
        return b
    }
}

private val DenoWasmServerStdinCode = /* language: javascript **/"""
// ISOMORPHIC: works on Deno and node
const isDeno = (typeof Deno) !== "undefined";
const debugEnv = isDeno ? Deno.env.get("DEBUG") : process.env
const debug = debugEnv == "true";
//const debug = true
const fs = (isDeno) ? null : require('fs');

function readSyncExact(reader, size) {
  if (size >= 32 * 1024 * 1024) throw new Error(`too big to read ${'$'}{size}`);
  var out = new Uint8Array(size);
  var pos = 0;
  while (pos < size) {
    const chunk = out.subarray(pos)
    var read = isDeno
      ? reader.readSync(chunk)
      : fs.readSync(reader, chunk);
    if (read == null || read <= 0) throw Error("Broken pipe");
    pos += read;
  }
  return out;
}

function writeSyncExact(writer, data) {
  var pos = 0;
  while (pos < data.length) {
    const chunk = data.subarray(pos)
    var written = isDeno
      ? writer.writeSync(chunk)
      : fs.writeSync(writer, chunk);
    if (written <= 0) throw Error("Broken pipe");
    pos += written;
  }
  return pos;
}

function readPacket(reader) {
  const [kind, streamId, len] = new Int32Array(
    (readSyncExact(reader, 12)).buffer,
  );
  const payload = readSyncExact(reader, len);
  if (debug) {
    console.error("   packet: ", "id:", streamId, "kind:", kind, "len:", len);
  }
  return { kind, streamId, payload };
}

function writePacket(writer, payload) {
  const data = new Uint8Array(4 + payload.length);
  const dview = new DataView(data.buffer);
  dview.setInt32(0, payload.length, true);
  data.set(payload, 4);
  const written = writeSyncExact(writer, data);
  if (written != data.length) {
    throw new Error(
      `Not written as many bytes as expected! ${'$'}{written}, but expected ${'$'}{data.length}`,
    );
  }
}

class WasmModule {
  constructor(
    exports,
    u8,
  ) {
    this.exports = exports
    this.u8 = u8
  }
}

const wasmModules = new Map();

function readAndProcessPacket(
  reader,
  writer,
) {
  const { kind, streamId, payload } = readPacket(reader);
  const wasmModule = wasmModules.get(streamId);
  // write bytes
  switch (kind) {
    case 1000: { // Load WASM module
      const wasmModule = new WebAssembly.Module(payload);
      if (debug) console.error("!!CMD: Loaded WASM... ", wasmModule);
      const wasmInstance = new WebAssembly.Instance(wasmModule, {
        "wasi_snapshot_preview1": {
          proc_exit: () => console.error(arguments),
          fd_close: () => console.error(arguments),
          fd_write: () => console.error(arguments),
          fd_seek: () => console.error(arguments),
        },
      });
      wasmModules.set(
        streamId,
        new WasmModule(
          wasmInstance.exports,
          new Uint8Array((wasmInstance.exports.memory).buffer),
        ),
      );
      writePacket(writer, new Uint8Array(0));
      break;
    }
    case 1001: { // Unload WASM module and close connection
      if (debug) console.error("!!CMD: Unload WASM...");
      wasmModules.delete(streamId);
      writePacket(writer, new Uint8Array(0));
      break;
    }
    case 1010: { // writeBytes
      const [offset] = new Int32Array(payload.buffer, 0, 1);
      const data = new Uint8Array(payload, 4);
      if (debug) console.error("!!CMD: Write Bytes...", offset, data.length);
      wasmModule?.u8?.set(data, offset);
      writePacket(writer, new Uint8Array(0));
      break;
    }
    case 1011: { // allocAndWrite
      let offset = 0;
      if (wasmModule) {
        offset = wasmModule.exports.malloc(payload.length);
        if (debug) {
          console.error("!!CMD: allocAndWrite...", offset, payload.length);
        }
        wasmModule.u8?.set(payload, offset);
      }
      writePacket(writer, new Uint8Array(new Int32Array([offset]).buffer));
      break;
    }
    case 1012: { // free
      if (wasmModule) {
        const ptrs = new Int32Array(payload.buffer);
        for (const ptr of ptrs) {
          if (ptr != 0) wasmModule.exports.free(ptr);
        }
        if (debug) console.error("!!CMD: free...", ptrs);
      }
      writePacket(writer, new Uint8Array(0));
      break;
    }
    case 1013: { // stackSave
      const result = wasmModule.exports.stackSave();
      if (debug) console.error("!!CMD: stackSave...", "->", result);
      writePacket(writer, new Uint8Array(new Int32Array([result]).buffer));
      break;
    }
    case 1014: { // stackRestore
      const [ptr] = new Int32Array(payload.buffer, 0, 1);
      wasmModule.exports.stackRestore(ptr);
      if (debug) console.error("!!CMD: stackRestore...", ptr);
      writePacket(writer, new Uint8Array(0));
      break;
    }
    case 1015: { // stackAlloc
      const [size] = new Int32Array(payload.buffer, 0, 1);
      const result = wasmModule.exports.stackAlloc(size);
      if (debug) console.error("!!CMD: stackAlloc...", size, "->", result);
      writePacket(writer, new Uint8Array(new Int32Array([result]).buffer));
      break;
    }
    case 1016: { // stackAllocWrite
      const ptr = wasmModule.exports.stackAlloc(payload.length);
      wasmModule?.u8?.set(payload, ptr);
      if (debug) console.error("!!CMD: stackAllocWrite...", payload.length, "->", ptr);
      writePacket(writer, new Uint8Array(new Int32Array([ptr]).buffer));
      break;
    }
    case 1020: { // readBytes
      const info = new Int32Array(payload.buffer);
      const offset = info[0];
      const len = info[1];
      if (debug) console.error("!!CMD: Read Bytes...", offset, len, payload);
      writePacket(
        writer,
        wasmModule
          ? new Uint8Array(wasmModule.u8.buffer, offset, len)
          : new Uint8Array(len),
      );
      break;
    }
    case 1030: { // executeFunction
      const json = JSON.parse(new TextDecoder().decode(payload));
      if (debug) console.error("!!CMD: executeFunction...", json);
      let result = null;
      try {
        if (json.indirect !== undefined) {
            result = wasmModule.exports.__indirect_function_table.get(json.indirect)(...json.params);
        } else {
            result = wasmModule.exports[json.func](...json.params);
        }
      } catch (e) {
        console.error(e);
        console.error(e.stack);
      }
      const resultPayload = new TextEncoder().encode(JSON.stringify(result));
      if (debug) console.error("    -->", result, resultPayload);
      writePacket(writer, resultPayload);
      break;
    }
    default:
      console.error(`!!ERROR: Invalid message: ${'$'}{kind}`);
  }
}

function unhandledrejection(event) {
  console.error(`UNHANDLED PROMISE REJECTION: ${'$'}{event.reason}`);
  console.error(event.reason.stack);
  event.preventDefault();
}

if (isDeno) {
  addEventListener("unhandledrejection", unhandledrejection);
} else {
  process.on("unhandledrejection", unhandledrejection);
}

let running = true;

while (running) {
  if (isDeno) {
    readAndProcessPacket(Deno.stdin, Deno.stdout);
  } else {
    // I think (pure conjecture) that process.stdin.fd is a getter that, when referenced, will put stdin in non-blocking mode (which is causing the error). When you use the file descriptor directly, you work around that.
    // https://stackoverflow.com/questions/40362369/stdin-read-fails-on-some-input
    //readAndProcessPacket(process.stdin.fd, process.stdout.fd);
    readAndProcessPacket(0, 1);
  }
}
""".trimIndent()

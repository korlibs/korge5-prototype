package korlibs.ffi

import com.sun.jna.Function
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer
import korlibs.datastructure.lock.Lock
import korlibs.io.serialization.json.Json
import korlibs.memory.readS32LE
import korlibs.memory.write32LE
import korlibs.memory.writeBytes
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import kotlin.reflect.KClass

/*
object WASMER {
    external fun wasm_byte_vec_new(wat: Memory, len: Int, s: Pointer): Int
    external fun wasm_byte_vec_delete(wat: Memory)

    init {
        //Native.register("/Users/soywiz/Downloads/wasmer-darwin-arm64/lib/libwasmer-headless.dylib")
        Native.register("/Users/soywiz/Downloads/wasmer-darwin-arm64/lib/libwasmer.dylib")
    }
}
*/

/*
open class DENOEval {
    companion object : DENOEval()

    val process = ProcessBuilder("deno", "repl")
        //.inheritIO()
        .also { it.redirectErrorStream(true) }
        .start()

    var pending = ""

    init {
        sendEval(
            """
            window.base64ToByteArray = function(base64) {
                var binaryString = atob(base64);
                var bytes = new Uint8Array(binaryString.length);
                for (var i = 0; i < binaryString.length; i++) bytes[i] = binaryString.charCodeAt(i);
                return bytes;
            }            
            window.byteArrayToBase64 = function(bytes) {
                var binaryString = "";
                for (var i = 0; i < bytes.length; i++) binaryString += String.fromCharCode(bytes[i] & 0xFF);
                //console.error('binaryString', binaryString);
                return btoa(binaryString);
            }            
        """.trimIndent()
        )
    }

    inner class WasmModule(val id: Int) {
        fun evalWASMFunction(name: String, vararg params: Any?): Any? =
            this@DENOEval.evalWASMFunction(id, name, *params)
        fun unload() = this@DENOEval.unload(id)
        fun writeBytes(ptr: Int, data: ByteArray) = this@DENOEval.writeBytes(id, ptr, data)
        fun readBytes(ptr: Int, len: Int): ByteArray = this@DENOEval.readBytes(id, ptr, len)
    }

    fun loadWASM(bytes: ByteArray): WasmModule {
        return WasmModule(sendEval(
            """
            const base64 = "${bytes.toBase64()}";
            const wasmBytes = base64ToByteArray(base64);
            const wasmModule = new WebAssembly.Module(wasmBytes);
            const wasmInstance = new WebAssembly.Instance(wasmModule, {
                "wasi_snapshot_preview1": {
                    "proc_exit": function() { console.error(arguments); },
                    fd_close: function() { console.error(arguments); },
                    fd_write: function() { console.error(arguments); },
                    fd_seek: function() { console.error(arguments); },
                }
            });
            window.lastId = window.lastId || 1;
            window.mem = window.mem || [];
            window.wasmExports = window.wasmExports || []; 
            const id = window.lastId++;
            window.wasmExports[id] = wasmInstance.exports;
            window.mem[id] = new Int8Array(wasmInstance.exports.memory.buffer);
            //console.error(window.wasmExports);
            console.error(id);
        """
        ).toInt())
    }

    fun evalWASMFunction(id: Int, name: String, vararg params: Any?): Any? {
        val paramsStr = params.joinToString(", ") { Json.stringify(it) }
        return Json.parse(
            sendEval(
                """
            console.error(JSON.stringify(window.wasmExports[$id].$name($paramsStr)))
        """
            )
        )
    }

    fun writeBytes(id: Int, ptr: Int, data: ByteArray) {
        sendEval(
            """
            const base64 = "${data.toBase64()}";
            const wasmBytes = base64ToByteArray(base64);
            const ptr = $ptr;
            const mem = window.mem[$id];
            for (let n = 0; n < wasmBytes.length; n++) mem[ptr + n] = wasmBytes[n];
        """
        )
    }

    fun readBytes(id: Int, ptr: Int, len: Int): ByteArray {
        //fun readMemory(ptr: Int, len: Int): String {
        val base64 = sendEval(
            """
            const wasmBytes = new Int8Array($len);
            const ptr = $ptr;
            const mem = window.mem[$id];
            for (let n = 0; n < wasmBytes.length; n++) wasmBytes[n] = mem[ptr + n];
            console.error(byteArrayToBase64(wasmBytes));
        """
        )
        return base64.fromBase64()
        //return base64
    }

    fun sendEval(code: String): String {
        val SOF = "<<<START_OF_COMMAND>>>"
        val EOF = "<<<END_OF_COMMAND>>>"
        val bytes =
            "console.error('$SOF'); try { eval(${code.quoted}) } catch (e) { console.error(e); } finally { console.error('$EOF'); }\n".toByteArray()
        process.outputStream.write(bytes)
        process.outputStream.flush()
        val data = StringBuilder()
        data.append(pending)
        val temp = ByteArray(10240)
        while (true) {
            val read = process.inputStream.read(temp, 0, temp.size)
            if (read <= 0) break
            val chunk = temp.copyOf(read).decodeToString()
            //println("READ: $read : chunk='$chunk'")
            data.append(chunk)
            if (data.contains(EOF)) {
                break
            }
        }
        val (message, pending) = arrayOf(*data.split(EOF).toTypedArray(), "")
        val (_, rmessage) = message.split(SOF)
        this.pending = pending
        return rmessage.trim()
    }

    fun unload(id: Int) {
        sendEval("delete window.mem[id]; delete window.wasmExports[id];")
    }

    fun close() {
        sendEval("Deno.exit(0)")
        process.destroy()
    }
}

 */

actual fun FFILibSym(lib: BaseLib): FFILibSym {
    return FFILibSymJVM(lib)
}

class FFILibSymJVM(val lib: BaseLib) : FFILibSym {
    val nlib by lazy {
        lib as FFILib
        lib.paths.firstNotNullOfOrNull {
            NativeLibrary.getInstance(it)
        }
    }

    val libWasm = lib as? WASMLib?

    val functions by lazy {
        lib.functions.associate { nfunc ->
            //val lib = NativeLibrary.getInstance("")
            nfunc.name to Proxy.newProxyInstance(
                this::class.java.classLoader,
                arrayOf((nfunc.type.classifier as KClass<*>).java),
                object : InvocationHandler {
                    val func: Function? = if (libWasm == null) nlib?.getFunction(nfunc.name) ?: error("Can't find function ${nfunc.name}") else null
                    override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
                        if (libWasm != null) {
                            val sargs = args ?: emptyArray()
                            //return wasm.evalWASMFunction(nfunc.name, *sargs)
                            return wasm.executeFunction(nfunc.name, *sargs)
                        } else {
                            //println("INVOKE: ${method.name} : ${func.name} : args=${args?.toList()}, ret=${nfunc.ret}")
                            return when (nfunc.ret) {
                                Unit::class -> func?.invokeVoid(args)
                                Int::class -> func?.invokeInt(args)
                                Float::class -> func?.invokeFloat(args)
                                Double::class -> func?.invokeDouble(args)
                                else -> func?.invoke((nfunc.ret as KClass<*>).java, args)
                            }
                        }
                    }
                })
        }
    }

    val wasm: DenoWASM by lazy {
        if (libWasm == null) error("Not a WASM module")
        DenoWasmProcessStdin.open(libWasm.content)
    }

    override fun <T> get(name: String): T = functions[name] as T
    override fun readBytes(pos: Int, size: Int): ByteArray = wasm.readBytes(pos, size)
    override fun writeBytes(pos: Int, data: ByteArray) = wasm.writeBytes(pos, data)
    override fun allocBytes(bytes: ByteArray): Int = wasm.allocAndWrite(bytes)
    override fun freeBytes(vararg ptrs: Int) = wasm.free(*ptrs)

    override fun close() {
        if (libWasm != null) {
            wasm.close()
        }
    }
}

actual typealias FFIPointer = Pointer

actual fun FFIPointer.getStringz(): String {
    return this.getString(0L)
}

actual val FFIPointer?.str: String get() = this.toString()
actual fun FFIPointer.readInts(size: Int, offset: Int): IntArray {
    return this.getIntArray(0L, size)
}

object DenoWasmProcessStdin {
    //val PORT = 9090
    val PORT = 8080
    //val file = File("/tmp/deno.wasm.stdin.deno.ts").also { it.writeText(DenoWasmServerStdinCode) }
    val file = File("/tmp/deno.wasm.stdin.deno.ts")
    val process = ProcessBuilder("deno", "run", "-A", "--unstable", file.absolutePath)
        .redirectError(File("/tmp/deno.wasm.stderr"))
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

/*
class DenoWasmServerSocket {
    //val PORT = 9090
    val PORT = 8080
    val file = File("/tmp/deno.wasm.server.deno.ts").also { it.writeText(DenoWasmServerSocketCode) }
    //val process = ProcessBuilder("sh", "-c", "nohup", "deno", "run", "-A", "--unstable", file.absolutePath)
    val process = ProcessBuilder("deno", "run", "-A", "--unstable", file.absolutePath)
        .also { it.redirectError(File("/tmp/deno.wasm.server.deno.ts.err")) }
        .also { it.redirectOutput(File("/tmp/deno.wasm.server.deno.ts.out")) }
        .also { it.environment()["PORT"] = "$PORT" }
        .also { it.environment()["HOST"] = "0.0.0.0" }
        .start()

    //init { Thread.sleep(50L) }

    //init {
    //    Runtime.getRuntime().addShutdownHook(Thread {
    //        close()
    //    })
    //}

    fun close() {
        process.destroy()
    }
}
*/

class DenoWASM(val io: DenoWasmIO, val streamId: Int, val wasmModuleBytes: ByteArray) : Closeable {
    init {
        io.loadWASM(streamId, wasmModuleBytes)
    }
    //fun open(bytes: ByteArray) { io.loadWASM(streamId, bytes) }

    fun writeBytes(ptr: Int, bytes: ByteArray) { io.writeBytes(streamId, ptr, bytes) }
    fun allocAndWrite(bytes: ByteArray): Int = io.allocAndWrite(streamId, bytes)
    fun free(vararg ptrs: Int) { io.free(streamId, *ptrs) }
    fun readBytes(ptr: Int, len: Int): ByteArray = io.readBytes(streamId, ptr, len)
    fun executeFunction(name: String, vararg params: Any?): Any? = io.executeFunction(streamId, name, *params)

    override fun close() { io.unloadWASM(streamId) }
}

class DenoWasmIO(val output: OutputStream, val input: InputStream, val close: Closeable? = null) {
    val debug = false
    //val debug = true

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

    fun readBytes(streamId: Int, ptr: Int, len: Int): ByteArray {
        val payload = ByteArray(8)
        payload.write32LE(0, ptr)
        payload.write32LE(4, len)
        if (debug) println("CMD:readBytes:ptr=$ptr,len=$len")
        return writeReadMessage(1020, streamId, payload)
    }

    fun executeFunction(streamId: Int, name: String, vararg params: Any?): Any? {
        val resultBytes = writeReadMessage(1030, streamId, Json.stringify(mapOf("func" to name, "params" to params.toList())).encodeToByteArray())
        val data = resultBytes.decodeToString()
        if (debug) println("CMD:executeFunction:name=$name, params=${params.toList()}")
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
    const debug = Deno.env.get("DEBUG") == "true";

    function readSyncExact(reader: Deno.ReaderSync, size: number): Uint8Array {
      if (size >= 32 * 1024 * 1024) throw new Error("too big to read");
      var out = new Uint8Array(size);
      var pos = 0;
      while (pos < size) {
        var read = reader.readSync(out.subarray(pos));
        if (read == null || read <= 0) throw Error("Broken pipe");
        pos += read;
      }
      return out;
    }

    function writeSyncExact(writer: Deno.WriterSync, data: Uint8Array): number {
      var pos = 0;
      while (pos < data.length) {
        var written = writer.writeSync(data.subarray(pos));
        if (written <= 0) throw Error("Broken pipe");
        pos += written;
      }
      return pos;
    }

    function readPacket(reader: Deno.ReaderSync) {
      const [kind, streamId, len] = new Int32Array(
        (readSyncExact(reader, 12)).buffer,
      );
      const payload = readSyncExact(reader, len);
      if (debug) {
        console.error("   packet: ", "id:", streamId, "kind:", kind, "len:", len);
      }
      return { kind, streamId, payload };
    }

    function writePacket(writer: Deno.WriterSync, payload: Uint8Array) {
      const data = new Uint8Array(4 + payload.length);
      const dview = new DataView(data.buffer);
      dview.setInt32(0, payload.length, true);
      data.set(payload, 4);
      const written = writeSyncExact(writer, data);
      if (written != data.length) {
        throw new Error(
          `Not written as many bytes as expected! ${"$"}{written}, but expected ${"$"}{data.length}`,
        );
      }
    }

    class WasmModule {
      constructor(
        public exports: any,
        public u8: Uint8Array,
      ) {
      }
    }

    const wasmModules = new Map<number, WasmModule>();

    function readAndProcessPacket(
      reader: Deno.ReaderSync,
      writer: Deno.WriterSync,
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
              new Uint8Array((<any> wasmInstance.exports.memory).buffer),
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
        case 1020: { // readBytes
          const info = new Int32Array(payload.buffer);
          const offset = info[0];
          const len = info[1];
          if (debug) console.error("!!CMD: Read Bytes...", offset, len, payload);
          writePacket(
            writer,
            wasmModule
              ? new Uint8Array(wasmModule.u8!.buffer, offset, len)
              : new Uint8Array(len),
          );
          break;
        }
        case 1030: { // executeFunction
          const json = JSON.parse(new TextDecoder().decode(payload));
          if (debug) console.error("!!CMD: executeFunction...", json);
          let result = null;
          try {
            result = wasmModule!.exports[json.func](...json.params);
          } catch (e) {
            console.warn(e);
            console.warn(e.stack);
          }
          const resultPayload = new TextEncoder().encode(JSON.stringify(result));
          writePacket(writer, resultPayload);
          break;
        }
        default:
          console.error(`!!ERROR: Invalid message: ${"$"}{kind}`);
      }
    }

    addEventListener("unhandledrejection", (event) => {
      console.warn(`UNHANDLED PROMISE REJECTION: ${"$"}{event.reason}`);
      console.warn(event.reason.stack);
      event.preventDefault();
    });

    let running = true;

    while (running) {
      readAndProcessPacket(Deno.stdin, Deno.stdout);
    }
""".trimIndent()

/*
private val DenoWasmServerSocketCode = """
    // deno run -A --unstable temp.ts
    
    const host = Deno.env.get("HOST") || '127.0.0.1';
    const port = Deno.env.get("PORT") || 8080;
    const listener = Deno.listen({ host: host, port: port });
    //const listener = Deno.listen({ path: "/tmp/deno.wasm.socket", transport: "unix" });
    console.error(`listening on ${'$'}{host}:${'$'}{port}`);
    
    const debug = false
    
    async function readExact(
      reader: Deno.Reader,
      count: number,
    ): Promise<Uint8Array> {
      const buf = new Uint8Array(count);
      let pos = 0;
      while (true) {
        const remaining = buf.length - pos;
        if (remaining <= 0) break;
        const readCount = await reader.read(
          new Uint8Array(buf.buffer, pos, remaining),
        );
        if (debug) console.error("readExact", readCount, count, pos, remaining);
        if (readCount == null) {
          //break;
          throw new Error("socket error");
        }
        pos += readCount;
      }
      return buf;
    }
    
    async function writeExact(writer: Deno.Writer, data: Uint8Array): Promise<number> {
        let offset = 0;
    
        while (offset < data.length) {
            const written = await writer.write(data.subarray(offset, data.length))
            if (written == null) throw new Error("Socket error");
            offset += written
        }
    
        return offset
    }
    
    async function readPacket(conn: Deno.Conn) {
      const [kind, len] = new Int32Array((await readExact(conn, 8)).buffer);
      const payload = await readExact(conn, len);
      if (debug) console.error("   packet: ", "kind:", kind, "len:", len);
      return { kind, payload };
    }
    
    async function writePacket(conn: Deno.Conn, payload: Uint8Array) {
      const data = new Uint8Array(4 + payload.length);
      const dview = new DataView(data.buffer);
      dview.setInt32(0, payload.length, true);
      data.set(payload, 4);
      const written = await writeExact(conn, data);
      if (written != data.length) {
        throw new Error(`Not written as many bytes as expected! ${'$'}{written}, but expected ${'$'}{data.length}`);
      }
    }
    
    async function handleConnection(conn: Deno.Conn) {
      let wasmExports: any = null;
      let wasmMemory = null;
      let wasmMemoryU8 = null;
      try {
        mainLoop:
        while (true) {
          const { kind, payload } = await readPacket(conn);
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
              wasmExports = wasmInstance.exports;
              wasmMemory = wasmExports.memory;
              wasmMemoryU8 = new Uint8Array((<any> wasmMemory).buffer);
              await writePacket(conn, new Uint8Array(0));
              break;
            }
            case 1001: { // Unload WASM module and close connection
                if (debug) console.error("!!CMD: Unload WASM...");
              wasmExports = null;
              wasmMemory = null;
              await writePacket(conn, new Uint8Array(0));
              conn.close();
              break mainLoop;
            }
            case 1010: { // writeBytes
              const [offset] = new Int32Array(payload.buffer, 0, 1);
              const data = new Uint8Array(payload, 4);
              if (debug) console.error("!!CMD: Write Bytes...", offset, data.length);
              wasmMemoryU8?.set(data, offset);
              await writePacket(conn, new Uint8Array(0));
              break;
            }
            case 1011: { // allocAndWrite
              const offset = wasmExports.malloc(payload.length);
              if (debug) console.error("!!CMD: allocAndWrite...", offset, payload.length);
              wasmMemoryU8?.set(payload, offset);
              await writePacket(
                conn,
                new Uint8Array(new Int32Array([offset]).buffer),
              );
              break;
            }
            case 1012: { // free
              const ptrs = new Int32Array(payload.buffer);
              for (const ptr of ptrs) {
                if (ptr != 0) wasmExports.free(ptr);
              }
              if (debug) console.error("!!CMD: free...", ptrs);
              await writePacket(conn, new Uint8Array(0));
              break;
            }
            case 1020: { // readBytes
              const info = new Int32Array(payload.buffer);
              const offset = info[0]
              const len = info[1];
              if (debug) console.error("!!CMD: Read Bytes...", offset, len, payload);
              await writePacket(
                conn,
                new Uint8Array(wasmMemoryU8!.buffer, offset, len),
              );
              break;
            }
            case 1030: { // executeFunction
              const json = JSON.parse(new TextDecoder().decode(payload));
              if (debug) console.error("!!CMD: executeFunction...", json);
              let result = null;
              try {
                result = wasmExports[json.func](...json.params);
              } catch (e) {
                console.warn(e);
                console.warn(e.stack);
              }
              const resultPayload = new TextEncoder().encode(
                JSON.stringify(result),
              );
              await writePacket(conn, resultPayload);
              break;
            }
            default:
              console.error(`!!ERROR: Invalid message: ${'$'}{kind}`);
              conn.close();
              break mainLoop;
          }
        }
      } catch (e) {
        console.warn(e);
      }
    }
    
    addEventListener("unhandledrejection", (event) => {
      console.warn(`UNHANDLED PROMISE REJECTION: ${'$'}{event.reason}`);
      console.warn(event.reason.stack);
      event.preventDefault();
    });
    
    for await (const conn of listener) {
      console.error("conn", conn);
      handleConnection(conn);
    }

""".trimIndent()

 */
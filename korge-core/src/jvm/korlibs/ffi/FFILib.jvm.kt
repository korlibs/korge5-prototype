package korlibs.ffi

import com.sun.jna.Function
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer
import korlibs.io.serialization.json.Json
import korlibs.memory.readS32LE
import korlibs.memory.write32LE
import korlibs.memory.writeBytes
import java.io.File
import java.io.InputStream
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.net.*
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
                //console.log('binaryString', binaryString);
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
                    "proc_exit": function() { console.log(arguments); },
                    fd_close: function() { console.log(arguments); },
                    fd_write: function() { console.log(arguments); },
                    fd_seek: function() { console.log(arguments); },
                }
            });
            window.lastId = window.lastId || 1;
            window.mem = window.mem || [];
            window.wasmExports = window.wasmExports || []; 
            const id = window.lastId++;
            window.wasmExports[id] = wasmInstance.exports;
            window.mem[id] = new Int8Array(wasmInstance.exports.memory.buffer);
            //console.log(window.wasmExports);
            console.log(id);
        """
        ).toInt())
    }

    fun evalWASMFunction(id: Int, name: String, vararg params: Any?): Any? {
        val paramsStr = params.joinToString(", ") { Json.stringify(it) }
        return Json.parse(
            sendEval(
                """
            console.log(JSON.stringify(window.wasmExports[$id].$name($paramsStr)))
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
            console.log(byteArrayToBase64(wasmBytes));
        """
        )
        return base64.fromBase64()
        //return base64
    }

    fun sendEval(code: String): String {
        val SOF = "<<<START_OF_COMMAND>>>"
        val EOF = "<<<END_OF_COMMAND>>>"
        val bytes =
            "console.log('$SOF'); try { eval(${code.quoted}) } catch (e) { console.log(e); } finally { console.log('$EOF'); }\n".toByteArray()
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

actual class FFILibSym actual constructor(val lib: BaseLib) {
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

    val wasm by lazy {
        if (libWasm == null) error("Not a WASM module")
        DenoWasmSocket().also {
            it.loadWASM(libWasm.content)
        }
    }
    /*
    val wasm: DENOEval.WasmModule by lazy {
        if (lib.kind != FFILibKind.WASM) error("Not a WASM module")
        DENOEval.loadWASM(lib.content!!)
    }

     */

    actual fun <T> get(name: String): T = functions[name] as T
    actual fun readBytes(pos: Int, size: Int): ByteArray = wasm.readBytes(pos, size)
    actual fun writeBytes(pos: Int, data: ByteArray) = wasm.writeBytes(pos, data)
    actual fun allocBytes(bytes: ByteArray): Int = wasm.allocAndWrite(bytes)
    actual fun freeBytes(vararg ptrs: Int) = wasm.free(*ptrs)

    fun unload() {
        if (libWasm != null) {
            wasm.unloadWASM()
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

object DenoWasmServerSocket {
    //val PORT = 9090
    val PORT = 8080
    val file = File("/tmp/deno.wasm.server.deno.ts").also { it.writeText(DenoWasmServerCode) }
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

class DenoWasmSocket {
    //val channel = SocketChannel.open(StandardProtocolFamily.UNIX)
    //channel.connect(socketAddress);
    //it.connect(UnixDomainSocketAddress.of("/tmp/deno.wasm.socket"))
    val socket = Socket().also {
        it.connect(InetSocketAddress(DenoWasmServerSocket.PORT))
        /*
        Thread.sleep(200L)
        for (n in 0 until 10) {
            try {
                it.connect(InetSocketAddress("127.0.0.1", DenoWasmServerSocket.PORT))
                break
            } catch (e: Throwable) {
                e.printStackTrace()
                Thread.sleep(100L + n * 10)
            }
        }

         */
    }

    val os = socket.getOutputStream()
    val iis = socket.getInputStream()

    val debug = false
    //val debug = true

    fun loadWASM(bytes: ByteArray) {
        writeReadMessage(1000, bytes)
        if (debug) println("CMD:loadWASM")
    }

    fun unloadWASM() {
        writeReadMessage(1001, byteArrayOf())
        if (debug) println("CMD:unloadWASM")
    }

    fun writeBytes(ptr: Int, bytes: ByteArray) {
        val payload = ByteArray(4 + bytes.size)
        payload.write32LE(0, ptr)
        payload.writeBytes(4, bytes)
        writeReadMessage(1010, payload)
        if (debug) println("CMD:writeBytes:ptr=$ptr, bytes=${bytes.size}")
    }

    fun allocAndWrite(bytes: ByteArray): Int {
        return writeReadMessage(1011, bytes).readS32LE(0)
    }
    fun free(vararg ptrs: Int) {
        val payload = ByteArray(ptrs.size * 4)
        for (n in 0 until ptrs.size) {
            payload.write32LE(n * 4, ptrs[n])
        }
        writeReadMessage(1012, payload)
        if (debug) println("CMD:free:ptrs=${ptrs.toList()}")
    }

    fun readBytes(ptr: Int, len: Int): ByteArray {
        val payload = ByteArray(8)
        payload.write32LE(0, ptr)
        payload.write32LE(4, len)
        if (debug) println("CMD:readBytes:ptr=$ptr,len=$len")
        return writeReadMessage(1020, payload)
    }

    fun executeFunction(name: String, vararg params: Any?): Any? {
        val resultBytes = writeReadMessage(1030, Json.stringify(mapOf("func" to name, "params" to params.toList())).encodeToByteArray())
        val data = resultBytes.decodeToString()
        if (debug) println("CMD:executeFunction:name=$name, params=${params.toList()}")
        return Json.parse(data)
    }

    private fun writeReadMessage(type: Int, payload: ByteArray): ByteArray {
        writeMessage(type, payload)
        return readMessage(type)
    }

    private fun writeMessage(type: Int, payload: ByteArray) {
        val buffer = ByteArray(8)
        buffer.write32LE(0, type)
        buffer.write32LE(4, payload.size)
        //println("writeMessage: type=$type, payload=${payload.size}")
        //socket.channel.write(arrayOf(buffer, ByteBuffer.wrap(payload)))
        os.write(buffer)
        os.write(payload)
        os.flush()
    }

    private fun readMessage(type: Int): ByteArray {
        //println(" -- readMessage type=$type")
        val sizeBuffer = ByteArray(4)
        iis.readBytesExact(sizeBuffer)
        val len = sizeBuffer.readS32LE(0)
        //println(" -- readMessage: len=$len")
        val out = ByteArray(len)
        iis.readBytesExact(out)
        //println(" --> ${out.size}")
        return out
    }

    private fun InputStream.readBytesExact(b: ByteArray): ByteArray {
        var offset = 0
        var remaining = b.size
        //println("readBytesExact[$remaining]")
        while (remaining > 0) {
            //println("   - READ $offset, $remaining")
            val len = read(b, offset, remaining)
            //println("      -> $len")
            if (len < 0) error("Socket error: len=$len")
            if (len <= 0) break
            offset += len
            remaining -= len
        }
        check(remaining == 0) { "remaining == 0 : $remaining" }
        //println("REMAINING: $remaining : ${b.toList()}")
        return b
    }

    private fun SocketChannel.readFully(b: ByteBuffer) {
        var remaining = b.remaining()
        while (remaining > 0) {
            val read = read(b)
            remaining -= read
        }
    }
}

private val DenoWasmServerCode = """
    // deno run -A --unstable temp.ts
    
    const host = Deno.env.get("HOST") || '127.0.0.1';
    const port = Deno.env.get("PORT") || 8080;
    const listener = Deno.listen({ host: host, port: port });
    //const listener = Deno.listen({ path: "/tmp/deno.wasm.socket", transport: "unix" });
    console.log(`listening on ${'$'}{host}:${'$'}{port}`);
    
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
        if (debug) console.log("readExact", readCount, count, pos, remaining);
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
      if (debug) console.log("   packet: ", "kind:", kind, "len:", len);
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
              if (debug) console.log("!!CMD: Loaded WASM... ", wasmModule);
              const wasmInstance = new WebAssembly.Instance(wasmModule, {
                "wasi_snapshot_preview1": {
                  proc_exit: () => console.log(arguments),
                  fd_close: () => console.log(arguments),
                  fd_write: () => console.log(arguments),
                  fd_seek: () => console.log(arguments),
                },
              });
              wasmExports = wasmInstance.exports;
              wasmMemory = wasmExports.memory;
              wasmMemoryU8 = new Uint8Array((<any> wasmMemory).buffer);
              await writePacket(conn, new Uint8Array(0));
              break;
            }
            case 1001: { // Unload WASM module and close connection
                if (debug) console.log("!!CMD: Unload WASM...");
              wasmExports = null;
              wasmMemory = null;
              await writePacket(conn, new Uint8Array(0));
              conn.close();
              break mainLoop;
            }
            case 1010: { // writeBytes
              const [offset] = new Int32Array(payload.buffer, 0, 1);
              const data = new Uint8Array(payload, 4);
              if (debug) console.log("!!CMD: Write Bytes...", offset, data.length);
              wasmMemoryU8?.set(data, offset);
              await writePacket(conn, new Uint8Array(0));
              break;
            }
            case 1011: { // allocAndWrite
              const offset = wasmExports.malloc(payload.length);
              if (debug) console.log("!!CMD: allocAndWrite...", offset, payload.length);
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
              if (debug) console.log("!!CMD: free...", ptrs);
              await writePacket(conn, new Uint8Array(0));
              break;
            }
            case 1020: { // readBytes
              const info = new Int32Array(payload.buffer);
              const offset = info[0]
              const len = info[1];
              if (debug) console.log("!!CMD: Read Bytes...", offset, len, payload);
              await writePacket(
                conn,
                new Uint8Array(wasmMemoryU8!.buffer, offset, len),
              );
              break;
            }
            case 1030: { // executeFunction
              const json = JSON.parse(new TextDecoder().decode(payload));
              if (debug) console.log("!!CMD: executeFunction...", json);
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
      console.log("conn", conn);
      handleConnection(conn);
    }

""".trimIndent()
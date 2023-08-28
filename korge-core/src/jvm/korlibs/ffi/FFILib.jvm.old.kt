

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
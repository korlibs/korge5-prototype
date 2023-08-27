package korlibs.memory.ffi

import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.reflect.KClass

actual class FFILibSym actual constructor(val lib: FFILib) {
    val nlib by lazy {
        lib.paths.firstNotNullOfOrNull {
            NativeLibrary.getInstance(it)
        }
    }

    val functions by lazy {
        lib.functions.associate { nfunc ->
            //val lib = NativeLibrary.getInstance("")
            nfunc.name to Proxy.newProxyInstance(
                this::class.java.classLoader,
                arrayOf((nfunc.type.classifier as KClass<*>).java),
                object : InvocationHandler {
                    val func = nlib?.getFunction(nfunc.name) ?: error("Can't find function ${nfunc.name}")
                    override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
                        //println("INVOKE: ${method.name} : ${func.name} : args=${args?.toList()}, ret=${nfunc.ret}")
                        return when (nfunc.ret) {
                            Unit::class -> func.invokeVoid(args)
                            Int::class -> func.invokeInt(args)
                            Float::class -> func.invokeFloat(args)
                            Double::class -> func.invokeDouble(args)
                            else -> func.invoke((nfunc.ret as KClass<*>).java, args)
                        }.also {
                            //println("    -> RESULT: $it")
                        }
                    }
                })
        }
    }

    actual fun <T> get(name: String): T {
        return functions[name] as T
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
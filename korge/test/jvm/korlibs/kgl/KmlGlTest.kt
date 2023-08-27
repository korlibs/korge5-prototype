package korlibs.kgl

import korlibs.image.bitmap.NativeImage
import korlibs.io.lang.quoted
import korlibs.memory.Buffer
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.valueParameters
import kotlin.reflect.javaType
import kotlin.test.Test

class KmlGlTest {
    @Test
    fun test() {
        fun Class<*>.toDenoFFI(ret: Boolean): String {
            return when (this) {
                //Long::class.javaObjectType, Long::class.javaPrimitiveType -> "pointer"
                Long::class.javaObjectType, Long::class.javaPrimitiveType -> "usize"
                Int::class.javaObjectType, Int::class.javaPrimitiveType -> "i32"
                Float::class.javaObjectType, Float::class.javaPrimitiveType -> "f32"
                Boolean::class.javaObjectType, Boolean::class.javaPrimitiveType -> "i8"
                NativeImage::class.java -> "buffer"
                Buffer::class.java -> "buffer"
                String::class.java -> if (ret) "pointer" else "buffer"
                Void::class.java, Unit::class.java -> "void"
                else -> TODO("$this")
            }
        }

        fun Class<*>.toKotlinStr(): String {
            return when (this) {
                //Long::class.javaObjectType, Long::class.javaPrimitiveType -> "pointer"
                Long::class.javaObjectType, Long::class.javaPrimitiveType -> "Long"
                Int::class.javaObjectType, Int::class.javaPrimitiveType -> "Int"
                Float::class.javaObjectType, Float::class.javaPrimitiveType -> "Float"
                Boolean::class.javaObjectType, Boolean::class.javaPrimitiveType -> "Boolean"
                NativeImage::class.java -> "NativeImage"
                Buffer::class.java -> "Buffer"
                String::class.java -> "String"
                Void::class.java, Unit::class.java -> "Unit"
                else -> TODO("$this")
            }
        }

        fun KType.toDenoFFI(ret: Boolean): String {
            return (this.classifier as KClass<*>).java.toDenoFFI(ret)
        }

        fun KType.toKotlinStr(): String {
            return (this.classifier as KClass<*>).java.toKotlinStr()
        }

        val DenoGL = StringBuilder()
        val DenoKmlGlBuilder = StringBuilder()

        DenoGL.appendLine("private val DenoGL = Deno.dlopen(\"/System/Library/Frameworks/OpenGL.framework/OpenGL\", jsObject(")
        DenoKmlGlBuilder.appendLine("class DenoKmlGl : KmlGl() {")
        DenoKmlGlBuilder.appendLine("  fun String.strBA(): ByteArray = \"\$this\\u0000\".encodeToByteArray()")
        DenoKmlGlBuilder.appendLine("  fun DenoPointer.ptrToStr(): String = this.readStringz()")
        DenoKmlGlBuilder.appendLine("  fun Boolean.toInt(): Int = if (this) 1 else 0")
        for (member in KmlGl::class.memberFunctions) {
            val name = member.name
            if (!member.isOpen) continue
            when (name) {
                "beforeDoRender" -> continue
                "startFrame", "endFrame" -> continue
                "init" -> continue
                "setExtra", "getExtra", "enableDisable" -> continue
                "handleContextLost" -> continue
                "setInfo", "getInfo" -> continue
                "equals", "hashCode", "toString" -> continue
                else -> Unit
            }
            val retType = member.returnType
            val params = member.valueParameters.toList()
            val glName = "gl${name.replaceFirstChar { it.uppercaseChar() }}"
            //println("$name -> $glName : $params : $retType")
            val denoParams = when (name) {
                "shaderSource" -> listOf("void", "i32", "pointer", "pointer")
                else -> (listOf(retType.toDenoFFI(ret = true)) + params.map { it.type.toDenoFFI(ret = false) }).map { it.quoted }
            }
            val kotlinParams = params.joinToString(", ") { "${it.name}: ${it.type.toKotlinStr()}" }
            val kotlinParamsCall = params.joinToString(", ") {
                when (it.type.classifier) {
                    String::class -> "${it.name}.strBA()"
                    Boolean::class -> "${it.name}.toInt()"
                    Long::class -> "${it.name}.toDouble()"
                    Buffer::class -> "${it.name}.dataView"
                    else -> "${it.name}"
                }
            }
            "hello\u0000".encodeToByteArray()
            //println("$name -> $glName : $denoParams : $retType")
            val suffix = when (retType.classifier) {
                String::class -> ".unsafeCast<DenoPointer>().ptrToStr()"
                else -> ""
            }
            val body = when (name) {
                "shaderSource" -> "{ val stringBytes = string.strBA(); DenoGL.glShaderSource(shader, 1, bigInt64ArrayOf(Deno.UnsafePointer.of(stringBytes.asDynamic()).value), bigInt64ArrayOf((stringBytes.size - 1).toLong().toJsBigInt())) }"
                else -> "= DenoGL.$glName($kotlinParamsCall)$suffix"
            }
            DenoKmlGlBuilder.appendLine("  override fun $name($kotlinParams): $retType $body")
            DenoGL.appendLine("  \"$glName\" to def(${denoParams.joinToString(", ")}),")
        }
        DenoKmlGlBuilder.appendLine("}")
        DenoGL.appendLine(")).symbols")

        println(DenoGL)
        println(DenoKmlGlBuilder)
    }
}
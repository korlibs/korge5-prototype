package korlibs.render.deno

import korlibs.ffi.*
import korlibs.io.file.sync.*
import korlibs.js.*
import korlibs.kgl.KmlGl
import korlibs.memory.Buffer

private fun def(result: dynamic, vararg params: dynamic): dynamic =
    korlibs.io.jsObject("parameters" to params, "result" to result)

private fun jsObject(vararg pairs: Pair<String, dynamic>): dynamic {
    val obj = js("{}")
    for ((key, value) in pairs) obj[key] = value
    return obj
}

@OptIn(SyncIOAPI::class)
internal val DenoGL = Deno.dlopen<dynamic>(
    LibraryResolver.resolve("OpenGL", "opengl32", "GL", "gl") ?: error("Can't find OpenGL library"), jsObject(
    "glActiveTexture" to def("void", "i32"),
    "glAttachShader" to def("void", "i32", "i32"),
    "glBindAttribLocation" to def("void", "i32", "i32", "buffer"),
    "glBindBuffer" to def("void", "i32", "i32"),
    "glBindBufferRange" to def("void", "i32", "i32", "i32", "i32", "i32"),
    "glBindFramebuffer" to def("void", "i32", "i32"),
    "glBindRenderbuffer" to def("void", "i32", "i32"),
    "glBindTexture" to def("void", "i32", "i32"),
    "glBindVertexArray" to def("void", "i32"),
    "glBlendColor" to def("void", "f32", "f32", "f32", "f32"),
    "glBlendEquation" to def("void", "i32"),
    "glBlendEquationSeparate" to def("void", "i32", "i32"),
    "glBlendFunc" to def("void", "i32", "i32"),
    "glBlendFuncSeparate" to def("void", "i32", "i32", "i32", "i32"),
    "glBufferData" to def("void", "i32", "i32", "buffer", "i32"),
    "glBufferSubData" to def("void", "i32", "i32", "i32", "buffer"),
    "glCheckFramebufferStatus" to def("i32", "i32"),
    "glClear" to def("void", "i32"),
    "glClearColor" to def("void", "f32", "f32", "f32", "f32"),
    "glClearDepthf" to def("void", "f32"),
    "glClearStencil" to def("void", "i32"),
    "glColorMask" to def("void", "i8", "i8", "i8", "i8"),
    "glCompileShader" to def("void", "i32"),
    "glCompressedTexImage2D" to def("void", "i32", "i32", "i32", "i32", "i32", "i32", "i32", "buffer"),
    "glCompressedTexSubImage2D" to def("void", "i32", "i32", "i32", "i32", "i32", "i32", "i32", "i32", "buffer"),
    "glCopyTexImage2D" to def("void", "i32", "i32", "i32", "i32", "i32", "i32", "i32", "i32"),
    "glCopyTexSubImage2D" to def("void", "i32", "i32", "i32", "i32", "i32", "i32", "i32", "i32"),
    "glCreateProgram" to def("i32"),
    "glCreateShader" to def("i32", "i32"),
    "glCullFace" to def("void", "i32"),
    "glDeleteBuffers" to def("void", "i32", "buffer"),
    "glDeleteFramebuffers" to def("void", "i32", "buffer"),
    "glDeleteProgram" to def("void", "i32"),
    "glDeleteRenderbuffers" to def("void", "i32", "buffer"),
    "glDeleteShader" to def("void", "i32"),
    "glDeleteTextures" to def("void", "i32", "buffer"),
    "glDeleteVertexArrays" to def("void", "i32", "buffer"),
    "glDepthFunc" to def("void", "i32"),
    "glDepthMask" to def("void", "i8"),
    "glDepthRangef" to def("void", "f32", "f32"),
    "glDetachShader" to def("void", "i32", "i32"),
    "glDisable" to def("void", "i32"),
    "glDisableVertexAttribArray" to def("void", "i32"),
    "glDrawArrays" to def("void", "i32", "i32", "i32"),
    "glDrawArraysInstanced" to def("void", "i32", "i32", "i32", "i32"),
    "glDrawElements" to def("void", "i32", "i32", "i32", "i32"),
    "glDrawElementsInstanced" to def("void", "i32", "i32", "i32", "i32", "i32"),
    "glEnable" to def("void", "i32"),
    "glEnableVertexAttribArray" to def("void", "i32"),
    "glFinish" to def("void"),
    "glFlush" to def("void"),
    "glFramebufferRenderbuffer" to def("void", "i32", "i32", "i32", "i32"),
    "glFramebufferTexture2D" to def("void", "i32", "i32", "i32", "i32", "i32"),
    "glFrontFace" to def("void", "i32"),
    "glGenBuffers" to def("void", "i32", "buffer"),
    "glGenFramebuffers" to def("void", "i32", "buffer"),
    "glGenRenderbuffers" to def("void", "i32", "buffer"),
    "glGenTextures" to def("void", "i32", "buffer"),
    "glGenVertexArrays" to def("void", "i32", "buffer"),
    "glGenerateMipmap" to def("void", "i32"),
    "glGetActiveAttrib" to def("void", "i32", "i32", "i32", "buffer", "buffer", "buffer", "buffer"),
    "glGetActiveUniform" to def("void", "i32", "i32", "i32", "buffer", "buffer", "buffer", "buffer"),
    "glGetAttachedShaders" to def("void", "i32", "i32", "buffer", "buffer"),
    "glGetAttribLocation" to def("i32", "i32", "buffer"),
    "glGetBooleanv" to def("void", "i32", "buffer"),
    "glGetBufferParameteriv" to def("void", "i32", "i32", "buffer"),
    "glGetError" to def("i32"),
    "glGetFloatv" to def("void", "i32", "buffer"),
    "glGetFramebufferAttachmentParameteriv" to def("void", "i32", "i32", "i32", "buffer"),
    "glGetIntegerv" to def("void", "i32", "buffer"),
    "glGetProgramInfoLog" to def("void", "i32", "i32", "buffer", "buffer"),
    "glGetProgramiv" to def("void", "i32", "i32", "buffer"),
    "glGetRenderbufferParameteriv" to def("void", "i32", "i32", "buffer"),
    "glGetShaderInfoLog" to def("void", "i32", "i32", "buffer", "buffer"),
    "glGetShaderPrecisionFormat" to def("void", "i32", "i32", "buffer", "buffer"),
    "glGetShaderSource" to def("void", "i32", "i32", "buffer", "buffer"),
    "glGetShaderiv" to def("void", "i32", "i32", "buffer"),
    "glGetString" to def("pointer", "i32"),
    "glGetStringi" to def("pointer", "i32", "i32"),
    "glGetTexParameterfv" to def("void", "i32", "i32", "buffer"),
    "glGetTexParameteriv" to def("void", "i32", "i32", "buffer"),
    "glGetUniformBlockIndex" to def("i32", "i32", "buffer"),
    "glGetUniformLocation" to def("i32", "i32", "buffer"),
    "glGetUniformfv" to def("void", "i32", "i32", "buffer"),
    "glGetUniformiv" to def("void", "i32", "i32", "buffer"),
    "glGetVertexAttribPointerv" to def("void", "i32", "i32", "buffer"),
    "glGetVertexAttribfv" to def("void", "i32", "i32", "buffer"),
    "glGetVertexAttribiv" to def("void", "i32", "i32", "buffer"),
    "glHint" to def("void", "i32", "i32"),
    "glIsBuffer" to def("i8", "i32"),
    "glIsEnabled" to def("i8", "i32"),
    "glIsFramebuffer" to def("i8", "i32"),
    "glIsProgram" to def("i8", "i32"),
    "glIsRenderbuffer" to def("i8", "i32"),
    "glIsShader" to def("i8", "i32"),
    "glIsTexture" to def("i8", "i32"),
    "glLineWidth" to def("void", "f32"),
    "glLinkProgram" to def("void", "i32"),
    "glPixelStorei" to def("void", "i32", "i32"),
    "glPolygonOffset" to def("void", "f32", "f32"),
    "glReadPixels" to def("void", "i32", "i32", "i32", "i32", "i32", "i32", "buffer"),
    "glReleaseShaderCompiler" to def("void"),
    "glRenderbufferStorage" to def("void", "i32", "i32", "i32", "i32"),
    "glRenderbufferStorageMultisample" to def("void", "i32", "i32", "i32", "i32", "i32"),
    "glSampleCoverage" to def("void", "f32", "i8"),
    "glScissor" to def("void", "i32", "i32", "i32", "i32"),
    "glShaderBinary" to def("void", "i32", "buffer", "i32", "buffer", "i32"),
    "glShaderSource" to def("void", "i32", "i32", "buffer", "buffer"),
    "glStencilFunc" to def("void", "i32", "i32", "i32"),
    "glStencilFuncSeparate" to def("void", "i32", "i32", "i32", "i32"),
    "glStencilMask" to def("void", "i32"),
    "glStencilMaskSeparate" to def("void", "i32", "i32"),
    "glStencilOp" to def("void", "i32", "i32", "i32"),
    "glStencilOpSeparate" to def("void", "i32", "i32", "i32", "i32"),
    //"glTexImage2D" to def("void", "i32", "i32", "i32", "i32", "i32", "buffer"),
    "glTexImage2D" to def("void", "i32", "i32", "i32", "i32", "i32", "i32", "i32", "i32", "buffer"),
    "glTexImage2DMultisample" to def("void", "i32", "i32", "i32", "i32", "i32", "i8"),
    "glTexParameterf" to def("void", "i32", "i32", "f32"),
    "glTexParameterfv" to def("void", "i32", "i32", "buffer"),
    "glTexParameteri" to def("void", "i32", "i32", "i32"),
    "glTexParameteriv" to def("void", "i32", "i32", "buffer"),
    "glTexSubImage2D" to def("void", "i32", "i32", "i32", "i32", "i32", "i32", "i32", "i32", "buffer"),
    "glUniform1f" to def("void", "i32", "f32"),
    "glUniform1fv" to def("void", "i32", "i32", "buffer"),
    "glUniform1i" to def("void", "i32", "i32"),
    "glUniform1iv" to def("void", "i32", "i32", "buffer"),
    "glUniform2f" to def("void", "i32", "f32", "f32"),
    "glUniform2fv" to def("void", "i32", "i32", "buffer"),
    "glUniform2i" to def("void", "i32", "i32", "i32"),
    "glUniform2iv" to def("void", "i32", "i32", "buffer"),
    "glUniform3f" to def("void", "i32", "f32", "f32", "f32"),
    "glUniform3fv" to def("void", "i32", "i32", "buffer"),
    "glUniform3i" to def("void", "i32", "i32", "i32", "i32"),
    "glUniform3iv" to def("void", "i32", "i32", "buffer"),
    "glUniform4f" to def("void", "i32", "f32", "f32", "f32", "f32"),
    "glUniform4fv" to def("void", "i32", "i32", "buffer"),
    "glUniform4i" to def("void", "i32", "i32", "i32", "i32", "i32"),
    "glUniform4iv" to def("void", "i32", "i32", "buffer"),
    "glUniformBlockBinding" to def("void", "i32", "i32", "i32"),
    "glUniformMatrix2fv" to def("void", "i32", "i32", "i8", "buffer"),
    "glUniformMatrix3fv" to def("void", "i32", "i32", "i8", "buffer"),
    "glUniformMatrix4fv" to def("void", "i32", "i32", "i8", "buffer"),
    "glUseProgram" to def("void", "i32"),
    "glValidateProgram" to def("void", "i32"),
    "glVertexAttrib1f" to def("void", "i32", "f32"),
    "glVertexAttrib1fv" to def("void", "i32", "buffer"),
    "glVertexAttrib2f" to def("void", "i32", "f32", "f32"),
    "glVertexAttrib2fv" to def("void", "i32", "buffer"),
    "glVertexAttrib3f" to def("void", "i32", "f32", "f32", "f32"),
    "glVertexAttrib3fv" to def("void", "i32", "buffer"),
    "glVertexAttrib4f" to def("void", "i32", "f32", "f32", "f32", "f32"),
    "glVertexAttrib4fv" to def("void", "i32", "buffer"),
    "glVertexAttribDivisor" to def("void", "i32", "i32"),
    "glVertexAttribPointer" to def("void", "i32", "i32", "i32", "i8", "i32", "usize"),
    "glViewport" to def("void", "i32", "i32", "i32", "i32"),
)).symbols

class DenoKmlGl : KmlGl() {
    fun String.strBA(): ByteArray = "$this\u0000".encodeToByteArray()
    fun DenoPointer.ptrToStr(): String = this.readStringz()
    fun Boolean.toInt(): Int = if (this) 1 else 0
    override fun activeTexture(texture: Int): Unit = DenoGL.glActiveTexture(texture)
    override fun attachShader(program: Int, shader: Int): Unit = DenoGL.glAttachShader(program, shader)
    override fun bindAttribLocation(program: Int, index: Int, name: String): Unit = DenoGL.glBindAttribLocation(program, index, name.strBA())
    override fun bindBuffer(target: Int, buffer: Int): Unit = DenoGL.glBindBuffer(target, buffer)
    override fun bindBufferRange(target: Int, index: Int, buffer: Int, offset: Int, size: Int): Unit = DenoGL.glBindBufferRange(target, index, buffer, offset, size)
    override fun bindFramebuffer(target: Int, framebuffer: Int): Unit = DenoGL.glBindFramebuffer(target, framebuffer)
    override fun bindRenderbuffer(target: Int, renderbuffer: Int): Unit = DenoGL.glBindRenderbuffer(target, renderbuffer)
    override fun bindTexture(target: Int, texture: Int): Unit = DenoGL.glBindTexture(target, texture)
    override fun bindVertexArray(array: Int): Unit = DenoGL.glBindVertexArray(array)
    override fun blendColor(red: Float, green: Float, blue: Float, alpha: Float): Unit = DenoGL.glBlendColor(red, green, blue, alpha)
    override fun blendEquation(mode: Int): Unit = DenoGL.glBlendEquation(mode)
    override fun blendEquationSeparate(modeRGB: Int, modeAlpha: Int): Unit = DenoGL.glBlendEquationSeparate(modeRGB, modeAlpha)
    override fun blendFunc(sfactor: Int, dfactor: Int): Unit = DenoGL.glBlendFunc(sfactor, dfactor)
    override fun blendFuncSeparate(sfactorRGB: Int, dfactorRGB: Int, sfactorAlpha: Int, dfactorAlpha: Int): Unit = DenoGL.glBlendFuncSeparate(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha)
    override fun bufferData(target: Int, size: Int, data: Buffer, usage: Int): Unit = DenoGL.glBufferData(target, size, data.dataView, usage)
    override fun bufferSubData(target: Int, offset: Int, size: Int, data: Buffer): Unit = DenoGL.glBufferSubData(target, offset, size, data.dataView)
    override fun checkFramebufferStatus(target: Int): kotlin.Int = DenoGL.glCheckFramebufferStatus(target)
    override fun clear(mask: Int): Unit = DenoGL.glClear(mask)
    override fun clearColor(red: Float, green: Float, blue: Float, alpha: Float): Unit = DenoGL.glClearColor(red, green, blue, alpha)
    override fun clearDepthf(d: Float): Unit = DenoGL.glClearDepthf(d)
    override fun clearStencil(s: Int): Unit = DenoGL.glClearStencil(s)
    override fun colorMask(red: Boolean, green: Boolean, blue: Boolean, alpha: Boolean): Unit = DenoGL.glColorMask(red.toInt(), green.toInt(), blue.toInt(), alpha.toInt())
    override fun compileShader(shader: Int): Unit = DenoGL.glCompileShader(shader)
    override fun compressedTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, imageSize: Int, data: Buffer): Unit = DenoGL.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data.dataView)
    override fun compressedTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, width: Int, height: Int, format: Int, imageSize: Int, data: Buffer): Unit = DenoGL.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data.dataView)
    override fun copyTexImage2D(target: Int, level: Int, internalformat: Int, x: Int, y: Int, width: Int, height: Int, border: Int): Unit = DenoGL.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border)
    override fun copyTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, x: Int, y: Int, width: Int, height: Int): Unit = DenoGL.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height)
    override fun createProgram(): kotlin.Int = DenoGL.glCreateProgram()
    override fun createShader(type: Int): kotlin.Int = DenoGL.glCreateShader(type)
    override fun cullFace(mode: Int): Unit = DenoGL.glCullFace(mode)
    override fun deleteBuffers(n: Int, items: Buffer): Unit = DenoGL.glDeleteBuffers(n, items.dataView)
    override fun deleteFramebuffers(n: Int, items: Buffer): Unit = DenoGL.glDeleteFramebuffers(n, items.dataView)
    override fun deleteProgram(program: Int): Unit = DenoGL.glDeleteProgram(program)
    override fun deleteRenderbuffers(n: Int, items: Buffer): Unit = DenoGL.glDeleteRenderbuffers(n, items.dataView)
    override fun deleteShader(shader: Int): Unit = DenoGL.glDeleteShader(shader)
    override fun deleteTextures(n: Int, items: Buffer): Unit = DenoGL.glDeleteTextures(n, items.dataView)
    override fun deleteVertexArrays(n: Int, arrays: Buffer): Unit = DenoGL.glDeleteVertexArrays(n, arrays.dataView)
    override fun depthFunc(func: Int): Unit = DenoGL.glDepthFunc(func)
    override fun depthMask(flag: Boolean): Unit = DenoGL.glDepthMask(flag.toInt())
    override fun depthRangef(n: Float, f: Float): Unit = DenoGL.glDepthRangef(n, f)
    override fun detachShader(program: Int, shader: Int): Unit = DenoGL.glDetachShader(program, shader)
    override fun disable(cap: Int): Unit = DenoGL.glDisable(cap)
    override fun disableVertexAttribArray(index: Int): Unit = DenoGL.glDisableVertexAttribArray(index)
    override fun drawArrays(mode: Int, first: Int, count: Int): Unit = DenoGL.glDrawArrays(mode, first, count)
    override fun drawArraysInstanced(mode: Int, first: Int, count: Int, instancecount: Int): Unit = DenoGL.glDrawArraysInstanced(mode, first, count, instancecount)
    override fun drawElements(mode: Int, count: Int, type: Int, indices: Int): Unit = DenoGL.glDrawElements(mode, count, type, indices)
    override fun drawElementsInstanced(mode: Int, count: Int, type: Int, indices: Int, instancecount: Int): Unit = DenoGL.glDrawElementsInstanced(mode, count, type, indices, instancecount)
    override fun enable(cap: Int): Unit = DenoGL.glEnable(cap)
    override fun enableVertexAttribArray(index: Int): Unit = DenoGL.glEnableVertexAttribArray(index)
    override fun finish(): Unit = DenoGL.glFinish()
    override fun flush(): Unit = DenoGL.glFlush()
    override fun framebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: Int): Unit = DenoGL.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer)
    override fun framebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: Int, level: Int): Unit = DenoGL.glFramebufferTexture2D(target, attachment, textarget, texture, level)
    override fun frontFace(mode: Int): Unit = DenoGL.glFrontFace(mode)
    override fun genBuffers(n: Int, buffers: Buffer): Unit = DenoGL.glGenBuffers(n, buffers.dataView)
    override fun genFramebuffers(n: Int, framebuffers: Buffer): Unit = DenoGL.glGenFramebuffers(n, framebuffers.dataView)
    override fun genRenderbuffers(n: Int, renderbuffers: Buffer): Unit = DenoGL.glGenRenderbuffers(n, renderbuffers.dataView)
    override fun genTextures(n: Int, textures: Buffer): Unit = DenoGL.glGenTextures(n, textures.dataView)
    override fun genVertexArrays(n: Int, arrays: Buffer): Unit = DenoGL.glGenVertexArrays(n, arrays.dataView)
    override fun generateMipmap(target: Int): Unit = DenoGL.glGenerateMipmap(target)
    override fun getActiveAttrib(program: Int, index: Int, bufSize: Int, length: Buffer, size: Buffer, type: Buffer, name: Buffer): Unit = DenoGL.glGetActiveAttrib(program, index, bufSize, length.dataView, size.dataView, type.dataView, name.dataView)
    override fun getActiveUniform(program: Int, index: Int, bufSize: Int, length: Buffer, size: Buffer, type: Buffer, name: Buffer): Unit = DenoGL.glGetActiveUniform(program, index, bufSize, length.dataView, size.dataView, type.dataView, name.dataView)
    override fun getAttachedShaders(program: Int, maxCount: Int, count: Buffer, shaders: Buffer): Unit = DenoGL.glGetAttachedShaders(program, maxCount, count.dataView, shaders.dataView)
    override fun getAttribLocation(program: Int, name: String): kotlin.Int = DenoGL.glGetAttribLocation(program, name.strBA())
    override fun getBooleanv(pname: Int, data: Buffer): Unit = DenoGL.glGetBooleanv(pname, data.dataView)
    override fun getBufferParameteriv(target: Int, pname: Int, params: Buffer): Unit = DenoGL.glGetBufferParameteriv(target, pname, params.dataView)
    override fun getError(): kotlin.Int = DenoGL.glGetError()
    override fun getFloatv(pname: Int, data: Buffer): Unit = DenoGL.glGetFloatv(pname, data.dataView)
    override fun getFramebufferAttachmentParameteriv(target: Int, attachment: Int, pname: Int, params: Buffer): Unit = DenoGL.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params.dataView)
    override fun getIntegerv(pname: Int, data: Buffer): Unit = DenoGL.glGetIntegerv(pname, data.dataView)
    override fun getProgramInfoLog(program: Int, bufSize: Int, length: Buffer, infoLog: Buffer): Unit = DenoGL.glGetProgramInfoLog(program, bufSize, length.dataView, infoLog.dataView)
    override fun getProgramiv(program: Int, pname: Int, params: Buffer): Unit = DenoGL.glGetProgramiv(program, pname, params.dataView)
    override fun getRenderbufferParameteriv(target: Int, pname: Int, params: Buffer): Unit = DenoGL.glGetRenderbufferParameteriv(target, pname, params.dataView)
    override fun getShaderInfoLog(shader: Int, bufSize: Int, length: Buffer, infoLog: Buffer): Unit = DenoGL.glGetShaderInfoLog(shader, bufSize, length.dataView, infoLog.dataView)
    override fun getShaderPrecisionFormat(shadertype: Int, precisiontype: Int, range: Buffer, precision: Buffer): Unit = DenoGL.glGetShaderPrecisionFormat(shadertype, precisiontype, range.dataView, precision.dataView)
    override fun getShaderSource(shader: Int, bufSize: Int, length: Buffer, source: Buffer): Unit = DenoGL.glGetShaderSource(shader, bufSize, length.dataView, source.dataView)
    override fun getShaderiv(shader: Int, pname: Int, params: Buffer): Unit = DenoGL.glGetShaderiv(shader, pname, params.dataView)
    override fun getString(name: Int): kotlin.String = DenoGL.glGetString(name).unsafeCast<DenoPointer>().ptrToStr()
    override fun getStringi(name: Int, index: Int): kotlin.String? = DenoGL.glGetStringi(name, index).unsafeCast<DenoPointer>().ptrToStr()
    override fun getTexParameterfv(target: Int, pname: Int, params: Buffer): Unit = DenoGL.glGetTexParameterfv(target, pname, params.dataView)
    override fun getTexParameteriv(target: Int, pname: Int, params: Buffer): Unit = DenoGL.glGetTexParameteriv(target, pname, params.dataView)
    override fun getUniformBlockIndex(program: Int, name: String): kotlin.Int = DenoGL.glGetUniformBlockIndex(program, name.strBA())
    override fun getUniformLocation(program: Int, name: String): kotlin.Int = DenoGL.glGetUniformLocation(program, name.strBA())
    override fun getUniformfv(program: Int, location: Int, params: Buffer): Unit = DenoGL.glGetUniformfv(program, location, params.dataView)
    override fun getUniformiv(program: Int, location: Int, params: Buffer): Unit = DenoGL.glGetUniformiv(program, location, params.dataView)
    override fun getVertexAttribPointerv(index: Int, pname: Int, pointer: Buffer): Unit = DenoGL.glGetVertexAttribPointerv(index, pname, pointer.dataView)
    override fun getVertexAttribfv(index: Int, pname: Int, params: Buffer): Unit = DenoGL.glGetVertexAttribfv(index, pname, params.dataView)
    override fun getVertexAttribiv(index: Int, pname: Int, params: Buffer): Unit = DenoGL.glGetVertexAttribiv(index, pname, params.dataView)
    override fun handleContextLost(): Unit = DenoGL.glHandleContextLost()
    override fun hint(target: Int, mode: Int): Unit = DenoGL.glHint(target, mode)
    override fun isBuffer(buffer: Int): kotlin.Boolean = DenoGL.glIsBuffer(buffer)
    override fun isEnabled(cap: Int): kotlin.Boolean = DenoGL.glIsEnabled(cap)
    override fun isFramebuffer(framebuffer: Int): kotlin.Boolean = DenoGL.glIsFramebuffer(framebuffer)
    override fun isProgram(program: Int): kotlin.Boolean = DenoGL.glIsProgram(program)
    override fun isRenderbuffer(renderbuffer: Int): kotlin.Boolean = DenoGL.glIsRenderbuffer(renderbuffer)
    override fun isShader(shader: Int): kotlin.Boolean = DenoGL.glIsShader(shader)
    override fun isTexture(texture: Int): kotlin.Boolean = DenoGL.glIsTexture(texture)
    override fun lineWidth(width: Float): Unit = DenoGL.glLineWidth(width)
    override fun linkProgram(program: Int): Unit = DenoGL.glLinkProgram(program)
    override fun pixelStorei(pname: Int, param: Int): Unit = DenoGL.glPixelStorei(pname, param)
    override fun polygonOffset(factor: Float, units: Float): Unit = DenoGL.glPolygonOffset(factor, units)
    override fun readPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, pixels: Buffer): Unit = DenoGL.glReadPixels(x, y, width, height, format, type, pixels.dataView)
    override fun releaseShaderCompiler(): Unit = DenoGL.glReleaseShaderCompiler()
    override fun renderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int): Unit = DenoGL.glRenderbufferStorage(target, internalformat, width, height)
    override fun renderbufferStorageMultisample(target: Int, samples: Int, internalformat: Int, width: Int, height: Int): Unit = DenoGL.glRenderbufferStorageMultisample(target, samples, internalformat, width, height)
    override fun sampleCoverage(value: Float, invert: Boolean): Unit = DenoGL.glSampleCoverage(value, invert.toInt())
    override fun scissor(x: Int, y: Int, width: Int, height: Int): Unit = DenoGL.glScissor(x, y, width, height)
    override fun shaderBinary(count: Int, shaders: Buffer, binaryformat: Int, binary: Buffer, length: Int): Unit = DenoGL.glShaderBinary(count, shaders.dataView, binaryformat, binary.dataView, length)
    override fun shaderSource(shader: Int, string: String): Unit {
        val stringBytes = string.strBA()
        DenoGL.glShaderSource(shader, 1, bigInt64ArrayOf(Deno.UnsafePointer.of(stringBytes.asDynamic()).value), bigInt64ArrayOf((stringBytes.size - 1).toLong().toJsBigInt()))
    }
    override fun stencilFunc(func: Int, ref: Int, mask: Int): Unit = DenoGL.glStencilFunc(func, ref, mask)
    override fun stencilFuncSeparate(face: Int, func: Int, ref: Int, mask: Int): Unit = DenoGL.glStencilFuncSeparate(face, func, ref, mask)
    override fun stencilMask(mask: Int): Unit = DenoGL.glStencilMask(mask)
    override fun stencilMaskSeparate(face: Int, mask: Int): Unit = DenoGL.glStencilMaskSeparate(face, mask)
    override fun stencilOp(fail: Int, zfail: Int, zpass: Int): Unit = DenoGL.glStencilOp(fail, zfail, zpass)
    override fun stencilOpSeparate(face: Int, sfail: Int, dpfail: Int, dppass: Int): Unit = DenoGL.glStencilOpSeparate(face, sfail, dpfail, dppass)
    override fun texImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, pixels: Buffer?): Unit {
        DenoGL.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels?.dataView)
        //Deno.exit(0)
    }
    override fun texImage2DMultisample(target: Int, samples: Int, internalformat: Int, width: Int, height: Int, fixedsamplelocations: Boolean): Unit = DenoGL.glTexImage2DMultisample(target, samples, internalformat, width, height, fixedsamplelocations.toInt())
    override fun texParameterf(target: Int, pname: Int, param: Float): Unit = DenoGL.glTexParameterf(target, pname, param)
    override fun texParameterfv(target: Int, pname: Int, params: Buffer): Unit = DenoGL.glTexParameterfv(target, pname, params.dataView)
    override fun texParameteri(target: Int, pname: Int, param: Int): Unit = DenoGL.glTexParameteri(target, pname, param)
    override fun texParameteriv(target: Int, pname: Int, params: Buffer): Unit = DenoGL.glTexParameteriv(target, pname, params.dataView)
    override fun texSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, width: Int, height: Int, format: Int, type: Int, pixels: Buffer): Unit = DenoGL.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels.dataView)
    override fun uniform1f(location: Int, v0: Float): Unit = DenoGL.glUniform1f(location, v0)
    override fun uniform1fv(location: Int, count: Int, value: Buffer): Unit = DenoGL.glUniform1fv(location, count, value.dataView)
    override fun uniform1i(location: Int, v0: Int): Unit = DenoGL.glUniform1i(location, v0)
    override fun uniform1iv(location: Int, count: Int, value: Buffer): Unit = DenoGL.glUniform1iv(location, count, value.dataView)
    override fun uniform2f(location: Int, v0: Float, v1: Float): Unit = DenoGL.glUniform2f(location, v0, v1)
    override fun uniform2fv(location: Int, count: Int, value: Buffer): Unit = DenoGL.glUniform2fv(location, count, value.dataView)
    override fun uniform2i(location: Int, v0: Int, v1: Int): Unit = DenoGL.glUniform2i(location, v0, v1)
    override fun uniform2iv(location: Int, count: Int, value: Buffer): Unit = DenoGL.glUniform2iv(location, count, value.dataView)
    override fun uniform3f(location: Int, v0: Float, v1: Float, v2: Float): Unit = DenoGL.glUniform3f(location, v0, v1, v2)
    override fun uniform3fv(location: Int, count: Int, value: Buffer): Unit = DenoGL.glUniform3fv(location, count, value.dataView)
    override fun uniform3i(location: Int, v0: Int, v1: Int, v2: Int): Unit = DenoGL.glUniform3i(location, v0, v1, v2)
    override fun uniform3iv(location: Int, count: Int, value: Buffer): Unit = DenoGL.glUniform3iv(location, count, value.dataView)
    override fun uniform4f(location: Int, v0: Float, v1: Float, v2: Float, v3: Float): Unit = DenoGL.glUniform4f(location, v0, v1, v2, v3)
    override fun uniform4fv(location: Int, count: Int, value: Buffer): Unit = DenoGL.glUniform4fv(location, count, value.dataView)
    override fun uniform4i(location: Int, v0: Int, v1: Int, v2: Int, v3: Int): Unit = DenoGL.glUniform4i(location, v0, v1, v2, v3)
    override fun uniform4iv(location: Int, count: Int, value: Buffer): Unit = DenoGL.glUniform4iv(location, count, value.dataView)
    override fun uniformBlockBinding(program: Int, uniformBlockIndex: Int, uniformBlockBinding: Int): Unit = DenoGL.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding)
    override fun uniformMatrix2fv(location: Int, count: Int, transpose: Boolean, value: Buffer): Unit = DenoGL.glUniformMatrix2fv(location, count, transpose.toInt(), value.dataView)
    override fun uniformMatrix3fv(location: Int, count: Int, transpose: Boolean, value: Buffer): Unit = DenoGL.glUniformMatrix3fv(location, count, transpose.toInt(), value.dataView)
    override fun uniformMatrix4fv(location: Int, count: Int, transpose: Boolean, value: Buffer): Unit = DenoGL.glUniformMatrix4fv(location, count, transpose.toInt(), value.dataView)
    override fun useProgram(program: Int): Unit = DenoGL.glUseProgram(program)
    override fun validateProgram(program: Int): Unit = DenoGL.glValidateProgram(program)
    override fun vertexAttrib1f(index: Int, x: Float): Unit = DenoGL.glVertexAttrib1f(index, x)
    override fun vertexAttrib1fv(index: Int, v: Buffer): Unit = DenoGL.glVertexAttrib1fv(index, v.dataView)
    override fun vertexAttrib2f(index: Int, x: Float, y: Float): Unit = DenoGL.glVertexAttrib2f(index, x, y)
    override fun vertexAttrib2fv(index: Int, v: Buffer): Unit = DenoGL.glVertexAttrib2fv(index, v.dataView)
    override fun vertexAttrib3f(index: Int, x: Float, y: Float, z: Float): Unit = DenoGL.glVertexAttrib3f(index, x, y, z)
    override fun vertexAttrib3fv(index: Int, v: Buffer): Unit = DenoGL.glVertexAttrib3fv(index, v.dataView)
    override fun vertexAttrib4f(index: Int, x: Float, y: Float, z: Float, w: Float): Unit = DenoGL.glVertexAttrib4f(index, x, y, z, w)
    override fun vertexAttrib4fv(index: Int, v: Buffer): Unit = DenoGL.glVertexAttrib4fv(index, v.dataView)
    override fun vertexAttribDivisor(index: Int, divisor: Int): Unit = DenoGL.glVertexAttribDivisor(index, divisor)
    override fun vertexAttribPointer(index: Int, size: Int, type: Int, normalized: Boolean, stride: Int, pointer: Long): Unit = DenoGL.glVertexAttribPointer(index, size, type, normalized.toInt(), stride, pointer.toDouble())
    override fun viewport(x: Int, y: Int, width: Int, height: Int): Unit = DenoGL.glViewport(x, y, width, height)
}


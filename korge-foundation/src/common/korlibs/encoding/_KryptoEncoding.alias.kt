@file:Suppress("PackageDirectoryMismatch")

package korlibs.crypto.encoding

@Deprecated("") typealias ASCII = korlibs.encoding.ASCII
@Deprecated("") fun String.fromAscii(): ByteArray = ASCII(this)
@Deprecated("") val ByteArray.ascii: String get() = ASCII(this)

@Deprecated("") typealias Base64 = korlibs.encoding.Base64
@Deprecated("") fun String.fromBase64IgnoreSpaces(url: Boolean = false): ByteArray = Base64.decode(this.replace(" ", "").replace("\n", "").replace("\r", ""), url)
@Deprecated("") fun String.fromBase64(ignoreSpaces: Boolean = false, url: Boolean = false): ByteArray = if (ignoreSpaces) Base64.decodeIgnoringSpaces(this, url) else Base64.decode(this, url)
@Deprecated("") fun ByteArray.toBase64(url: Boolean = false, doPadding: Boolean = false): String = Base64.encode(this, url, doPadding)
@Deprecated("") val ByteArray.base64: String get() = Base64.encode(this)
@Deprecated("") val ByteArray.base64Url: String get() = Base64.encode(this, true)

@Deprecated("") typealias Hex = korlibs.encoding.Hex

@Deprecated("") fun Appendable.appendHexByte(value: Int) = Hex.appendHexByte(this, value)
@Deprecated("") fun String.fromHex(): ByteArray = Hex.decode(this)
@Deprecated("") val ByteArray.hexLower: String get() = Hex.encodeLower(this)
@Deprecated("") val ByteArray.hexUpper: String get() = Hex.encodeUpper(this)
@Deprecated("") fun Char.isHexDigit() = Hex.isHexDigit(this)
@Deprecated("") val List<String>.unhexIgnoreSpaces get() = joinToString("").unhexIgnoreSpaces
@Deprecated("") val String.unhexIgnoreSpaces: ByteArray get() = buildString {
    val str = this@unhexIgnoreSpaces
    for (n in 0 until str.length) {
        val c = str[n]
        if (c != ' ' && c != '\t' && c != '\n' && c != '\r') append(c)
    }
}.unhex
@Deprecated("") val String.unhex get() = Hex.decode(this)
@Deprecated("") val ByteArray.hex get() = Hex.encodeLower(this)
@Deprecated("") val Int.hex: String get() = "0x$shex"
@Deprecated("") val Int.shex: String
    get() = buildString(8) {
        for (n in 0 until 8) {
            val v = (this@shex ushr ((7 - n) * 4)) and 0xF
            append(Hex.encodeCharUpper(v))
        }
    }
@Deprecated("") val Byte.hex: String get() = "0x$shex"
@Deprecated("") val Byte.shex: String
    get() = buildString(2) {
        append(Hex.encodeCharUpper((this@shex.toInt() ushr 4) and 0xF))
        append(Hex.encodeCharUpper((this@shex.toInt() ushr 0) and 0xF))
    }

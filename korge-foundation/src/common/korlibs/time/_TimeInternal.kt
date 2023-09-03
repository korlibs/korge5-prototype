@file:Suppress("PackageDirectoryMismatch")

package korlibs.time.internal

import korlibs.time.*
import korlibs.time.hr.HRTimeSpan
import korlibs.time.internal.clamp
import kotlin.jvm.JvmInline
import kotlin.math.*

internal inline fun Int.chainComparison(comparer: () -> Int): Int = if (this == 0) comparer() else this

internal inline fun <T> List<T>.fastForEach(callback: (T) -> Unit) {
    var n = 0
    while (n < size) callback(this[n++])
}

// Original implementation grabbed from Kds to prevent additional dependencies:
// - https://github.com/korlibs/kds/blob/965f6017d7ad82e4bad714acf26cd7189186bdb3/kds/src/commonMain/kotlin/korlibs/datastructure/_Extensions.kt#L48
internal inline fun genericBinarySearch(
    fromIndex: Int,
    toIndex: Int,
    invalid: (from: Int, to: Int, low: Int, high: Int) -> Int = { from, to, low, high -> -low - 1 },
    check: (index: Int) -> Int
): Int {
    var low = fromIndex
    var high = toIndex - 1

    while (low <= high) {
        val mid = (low + high) / 2
        val mval = check(mid)

        when {
            mval < 0 -> low = mid + 1
            mval > 0 -> high = mid - 1
            else -> return mid
        }
    }
    return invalid(fromIndex, toIndex, low, high)
}

@JvmInline
internal value class BSearchResult(val raw: Int) {
    val found: Boolean get() = raw >= 0
    val index: Int get() = if (found) raw else -1
    val nearIndex: Int get() = if (found) raw else -raw - 1
}


internal const val MILLIS_PER_SECOND = 1000
internal const val MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60 // 60_000
internal const val MILLIS_PER_HOUR = MILLIS_PER_MINUTE * 60 // 3600_000
internal const val MILLIS_PER_DAY = MILLIS_PER_HOUR * 24 // 86400_000
internal const val MILLIS_PER_WEEK = MILLIS_PER_DAY * 7 // 604800_000

internal fun Int.padded(count: Int): String {
    // @TODO: Handle edge case Int.MIN_VALUE that could not be represented as abs
    val res = this.absoluteValue.toString().padStart(count, '0')
    return if (this < 0) return "-$res" else res
}
internal fun Double.padded(intCount: Int, decCount: Int): String {
    val intPart = floor(this).toInt()
    val decPart = round((this - intPart) * 10.0.pow(decCount)).toInt()
    return "${intPart.padded(intCount).substr(-intCount, intCount)}.${decPart.toString().padStart(decCount, '0').substr(-decCount)}"
}

internal fun String.substr(start: Int, length: Int = this.length): String {
    val low = (if (start >= 0) start else this.length + start).clamp(0, this.length)
    val high = (if (length >= 0) low + length else this.length + length).clamp(0, this.length)
    return if (high < low) "" else this.substring(low, high)
}

internal fun Int.clamp(min: Int, max: Int): Int = if (this < min) min else if (this > max) max else this
internal fun Int.cycle(min: Int, max: Int): Int = ((this - min) umod (max - min + 1)) + min
internal fun Int.cycleSteps(min: Int, max: Int): Int = (this - min) / (max - min + 1)

internal fun String.splitKeep(regex: Regex): List<String> {
    val str = this
    val out = arrayListOf<String>()
    var lastPos = 0
    for (part in regex.findAll(this)) {
        val prange = part.range
        if (lastPos != prange.start) {
            out += str.substring(lastPos, prange.start)
        }
        out += str.substring(prange)
        lastPos = prange.endInclusive + 1
    }
    if (lastPos != str.length) {
        out += str.substring(lastPos)
    }
    return out
}

internal infix fun Int.umod(that: Int): Int {
    val remainder = this % that
    return when {
        remainder < 0 -> remainder + that
        else -> remainder
    }
}

internal infix fun Double.umod(that: Double): Double {
    val remainder = this % that
    return when {
        remainder < 0 -> remainder + that
        else -> remainder
    }
}

internal fun Double.toInt2(): Int = if (this < 0.0) floor(this).toInt() else this.toInt()
internal fun Double.toIntMod(mod: Int): Int = (this umod mod.toDouble()).toInt2()

internal infix fun Int.div2(other: Int): Int = when {
    this < 0 || this % other == 0 -> this / other
    else -> (this / other) - 1
}

internal class Moduler(val value: Double) {
    private var avalue = abs(value)
    private val sign = sign(value)

    fun double(count: Double): Double {
        val ret = (avalue / count)
        avalue %= count
        return floor(ret) * sign
    }
    fun double(count: Int): Double = double(count.toDouble())
    fun double(count: Float): Double = double(count.toDouble())

    fun int(count: Double): Int = double(count).toInt()
    fun int(count: Int): Int = int(count.toDouble())
    fun int(count: Float): Int = int(count.toDouble())
}

internal infix fun Double.intDiv(other: Double) = floor(this / other)

internal expect object KlockInternal {
    val currentTime: Double
    val hrNow: HRTimeSpan
    fun localTimezoneOffsetMinutes(time: DateTime): TimeSpan
    fun sleep(time: HRTimeSpan)
}

expect interface Serializable

internal fun <K> MutableMap<K, Int>.increment(key: K) {
    this.getOrPut(key) { 0 }
    this[key] = this[key]!! + 1
}

internal class MicroStrReader(val str: String, var offset: Int = 0) {
    val length get() = str.length
    val available get() = str.length - offset
    val hasMore get() = offset < str.length
    val eof get() = !hasMore
    inline fun readChunk(callback: () -> Unit): String {
        val start = this.offset
        callback()
        val end = this.offset
        return this.str.substring(start, end)
    }
    fun peekCharOrZero(): Char = if (hasMore) str[offset] else '\u0000'
    fun peekChar(): Char = str[offset]
    fun readChar(): Char = str[offset++]
    fun tryRead(expected: Char): Boolean {
        if (eof || peekChar() != expected) return false
        readChar()
        return true
    }
    fun tryReadOrNull(expected: String): String? {
        return if (tryRead(expected)) expected else null
    }
    fun tryRead(expected: String): Boolean {
        if (expected.length > available) return false
        for (n in expected.indices) if (this.str[offset + n] != expected[n]) return false
        offset += expected.length
        return true
    }
    fun read(count: Int): String = this.str.substring(offset, (offset + count).coerceAtMost(length)).also { this.offset += it.length }
    fun readRemaining(): String = read(available)
    fun readInt(count: Int): Int = read(count).toInt()
    fun tryReadInt(count: Int): Int? = read(count).toIntOrNull()
    fun tryReadDouble(count: Int): Double? = read(count).replace(',', '.').toDoubleOrNull()

    fun tryReadDouble(): Double? {
        var numCount = 0
        var num = 0
        var denCount = 0
        var den = 0
        var decimals = false
        loop@while (hasMore) {
            when (val pc = peekChar()) {
                ',' -> {
                    if (numCount == 0) {
                        return null
                    }
                    decimals = true
                    readChar()
                }
                in '0'..'9' -> {
                    val c = readChar()
                    if (decimals) {
                        denCount++
                        den *= 10
                        den += (c - '0')
                    } else {
                        numCount++
                        num *= 10
                        num += (c - '0')
                    }
                }
                else -> {
                    break@loop
                }
            }
        }
        if (numCount == 0) {
            return null
        }
        return num.toDouble() + (den.toDouble() * 10.0.pow(-denCount))
    }
}

internal val Double.niceStr: String get() = if (floor(this) == this) "${this.toInt()}" else "$this"

internal fun spinlock(time: HRTimeSpan) {
    val start = HRTimeSpan.now()
    while (HRTimeSpan.now() - start < time) Unit
}

internal fun MicroStrReader.readTimeZoneOffset(tzNames: TimezoneNames = TimezoneNames.DEFAULT): TimeSpan? {
    val reader = this
    for ((name, offset) in tzNames.namesToOffsets) {
        if (name == "GMT" || name == "UTC") continue
        if (reader.tryRead(name)) return offset
    }
    if (reader.tryRead('Z')) return 0.minutes
    var sign = +1
    reader.tryRead("GMT")
    reader.tryRead("UTC")
    if (reader.tryRead("+")) sign = +1
    if (reader.tryRead("-")) sign = -1
    val part = reader.readRemaining().replace(":", "")
    val hours = part.substr(0, 2).padStart(2, '0').toIntOrNull() ?: return null
    val minutes = part.substr(2, 2).padStart(2, '0').toIntOrNull() ?: return null
    val roffset = hours.hours + minutes.minutes
    return if (sign > 0) +roffset else -roffset
}
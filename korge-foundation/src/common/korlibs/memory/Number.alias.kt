package korlibs.memory

import korlibs.number.Fixed
import korlibs.number.FixedLong
import korlibs.number.FixedShort
import korlibs.number.Half
import korlibs.number.Int53
import kotlin.math.*

@Deprecated("") typealias Fixed = korlibs.number.Fixed
@Deprecated("") typealias FixedLong = korlibs.number.FixedLong
@Deprecated("") typealias FixedShort = korlibs.number.FixedShort
@Deprecated("") typealias Half = korlibs.number.Half
@Deprecated("") typealias Int53 = korlibs.number.Int53

@Deprecated("") val String.fixed: Fixed get() = Fixed(this)
@Deprecated("") val Long.fixed: Fixed get() = Fixed(this.toInt())
@Deprecated("") val Int.fixed: Fixed get() = Fixed(this)
@Deprecated("") val Double.fixed: Fixed get() = Fixed(this)
@Deprecated("") val Float.fixed: Fixed get() = Fixed(this.toDouble())
@Deprecated("") inline val Number.fixed: Fixed get() = Fixed(this.toDouble())

@Deprecated("") fun String.toFixed(): Fixed = Fixed(this)
@Deprecated("") fun Long.toFixed(): Fixed = Fixed(this.toInt())
@Deprecated("") fun Int.toFixed(): Fixed = Fixed(this)
@Deprecated("") fun Double.toFixed(): Fixed = Fixed(this)
@Deprecated("") fun Float.toFixed(): Fixed = Fixed(this.toDouble())
@Deprecated("") inline fun Number.toFixed(): Fixed = Fixed(this.toDouble())

@Deprecated("") val String.fixedLong: FixedLong get() = FixedLong(this)
@Deprecated("") val Long.fixedLong: FixedLong get() = FixedLong(this)
@Deprecated("") val Int.fixedLong: FixedLong get() = FixedLong(this.toLong())
@Deprecated("") val Double.fixedLong: FixedLong get() = FixedLong(this)
@Deprecated("") val Float.fixedLong: FixedLong get() = FixedLong(this.toDouble())
@Deprecated("") inline val Number.fixedLong: FixedLong get() = FixedLong(this.toDouble())

@Deprecated("") fun String.toFixedLong(): FixedLong = FixedLong(this)
@Deprecated("") fun Long.toFixedLong(): FixedLong = FixedLong(this)
@Deprecated("") fun Int.toFixedLong(): FixedLong = FixedLong(this.toLong())
@Deprecated("") fun Double.toFixedLong(): FixedLong = FixedLong(this)
@Deprecated("") fun Float.toFixedLong(): FixedLong = FixedLong(this.toDouble())
@Deprecated("") inline fun Number.toFixedLong(): FixedLong = FixedLong(this.toDouble())

@Deprecated("") fun String.toFixedShort(): FixedShort = FixedShort(this)
@Deprecated("") fun Long.toFixedShort(): FixedShort = FixedShort(this.toInt())
@Deprecated("") fun Int.toFixedShort(): FixedShort = FixedShort(this.toInt())
@Deprecated("") fun Short.toFixedShort(): FixedShort = FixedShort(this.toInt())
@Deprecated("") fun Double.toFixedShort(): FixedShort = FixedShort(this)
@Deprecated("") fun Float.toFixedShort(): FixedShort = FixedShort(this.toDouble())
@Deprecated("") inline fun Number.toFixedShort(): FixedShort = FixedShort(this.toDouble())

@Deprecated("") fun Int.toHalf(): Half = Half(this.toFloat())
@Deprecated("") fun Double.toHalf(): Half = Half(this)
@Deprecated("") fun Float.toHalf(): Half = Half(this)
@Deprecated("", ReplaceWith("this")) fun Half.toHalf(): Half = this
@Deprecated("") inline fun Number.toHalf(): Half = Half(this.toFloat())

@KmemExperimental
@Deprecated("") public inline fun String.toInt53(): Int53 = this.toDouble().toInt53()
@KmemExperimental
@Deprecated("") public inline fun String.toInt53OrNull(): Int53? = this.toDoubleOrNull()?.toInt53()
@KmemExperimental
@Deprecated("") public inline fun Int.toInt53(): Int53 = Int53.fromDoubleClamped(this.toDouble())
@KmemExperimental
@Deprecated("") public inline fun Double.toInt53(): Int53 = Int53.fromDoubleClamped(this)
@KmemExperimental
@Deprecated("") public inline fun Long.toInt53(): Int53 = Int53.fromDoubleClamped(this.toDouble())
@KmemExperimental
@Deprecated("") public inline fun Number.toInt53(): Int53 = Int53.fromDoubleClamped(this.toDouble())

/*
////////////////////
////////////////////

/** Converts this [Boolean] into integer: 1 for true, 0 for false */
@Deprecated("") public inline fun Boolean.toInt(): Int = if (this) 1 else 0
@Deprecated("") public inline fun Boolean.toByte(): Byte = if (this) 1 else 0

@Deprecated("") public inline fun Byte.toBoolean(): Boolean = this.toInt() != 0

////////////////////
////////////////////

/** Converts [this] into [Int] rounding to the ceiling */
@Deprecated("") public fun Float.toIntCeil(): Int = ceil(this).toInt()
/** Converts [this] into [Int] rounding to the ceiling */
@Deprecated("") public fun Double.toIntCeil(): Int = ceil(this).toInt()

/** Converts [this] into [Int] rounding to the floor */
@Deprecated("") public fun Float.toIntFloor(): Int = floor(this).toInt()
/** Converts [this] into [Int] rounding to the floor */
@Deprecated("") public fun Double.toIntFloor(): Int = floor(this).toInt()

/** Converts [this] into [Int] rounding to the nearest */
@Deprecated("") public fun Float.toIntRound(): Int = round(this).toInt()
/** Converts [this] into [Int] rounding to the nearest */
@Deprecated("") public fun Double.toIntRound(): Int = round(this).toInt()

/** Converts [this] into [Int] rounding to the nearest */
@Deprecated("") public fun Float.toLongRound(): Long = round(this).toLong()
/** Converts [this] into [Int] rounding to the nearest */
@Deprecated("") public fun Double.toLongRound(): Long = round(this).toLong()

/** Convert this [Long] into an [Int] but throws an [IllegalArgumentException] in the case that operation would produce an overflow */
@Deprecated("") public fun Long.toIntSafe(): Int = if (this in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) this.toInt() else throw IllegalArgumentException("Long doesn't fit Integer")

////////////////////
////////////////////

/** Returns an [Int] representing this [Byte] as if it was unsigned 0x00..0xFF */
public inline val Byte.unsigned: Int get() = this.toInt() and 0xFF

/** Returns a [Long] representing this [Int] as if it was unsigned 0x00000000L..0xFFFFFFFFL */
public inline val Int.unsigned: Long get() = this.toLong() and 0xFFFFFFFFL

////////////////////
////////////////////

/** Performs a fast integral logarithmic of base two */
@Deprecated("") public fun ilog2(v: Int): Int = if (v == 0) (-1) else (31 - v.countLeadingZeros())

////////////////////
////////////////////

/** Divides [this] into [that] rounding to the floor */
@Deprecated("") public infix fun Int.divFloor(that: Int): Int = this / that
/** Divides [this] into [that] rounding to the ceil */
@Deprecated("") public infix fun Int.divCeil(that: Int): Int = if (this % that != 0) (this / that) + 1 else (this / that)
/** Divides [this] into [that] rounding to the round */
@Deprecated("") public infix fun Int.divRound(that: Int): Int = (this.toDouble() / that.toDouble()).roundToInt()

////////////////////
////////////////////

/** Converts this value considering it was in the range [srcMin]..[srcMax] into [dstMin]..[dstMax], if the value is not inside the range the output value will be outside the destination range */
@Deprecated("") public fun Float.convertRange(srcMin: Float, srcMax: Float, dstMin: Float, dstMax: Float): Float = (dstMin + (dstMax - dstMin) * ((this - srcMin) / (srcMax - srcMin)))
/** Converts this value considering it was in the range [srcMin]..[srcMax] into [dstMin]..[dstMax], if the value is not inside the range the output value will be outside the destination range */
@Deprecated("") public fun Double.convertRange(srcMin: Double, srcMax: Double, dstMin: Double, dstMax: Double): Double = (dstMin + (dstMax - dstMin) * ((this - srcMin) / (srcMax - srcMin)))
//fun Double.convertRange(minSrc: Double, maxSrc: Double, minDst: Double, maxDst: Double): Double = (((this - minSrc) / (maxSrc - minSrc)) * (maxDst - minDst)) + minDst
/** Converts this value considering it was in the range [srcMin]..[srcMax] into [dstMin]..[dstMax], if the value is not inside the range the output value will be outside the destination range */
@Deprecated("") public fun Int.convertRange(srcMin: Int, srcMax: Int, dstMin: Int, dstMax: Int): Int = (dstMin + (dstMax - dstMin) * ((this - srcMin).toDouble() / (srcMax - srcMin).toDouble())).toInt()
/** Converts this value considering it was in the range [srcMin]..[srcMax] into [dstMin]..[dstMax], if the value is not inside the range the output value will be outside the destination range */
@Deprecated("") public fun Long.convertRange(srcMin: Long, srcMax: Long, dstMin: Long, dstMax: Long): Long = (dstMin + (dstMax - dstMin) * ((this - srcMin).toDouble() / (srcMax - srcMin).toDouble())).toLong()

/** Converts this value considering it was in the range [srcMin]..[srcMax] into [dstMin]..[dstMax], if the value is not inside the range the output value will be clamped to the nearest bound */
@Deprecated("") public fun Float.convertRangeClamped(srcMin: Float, srcMax: Float, dstMin: Float, dstMax: Float): Float = convertRange(srcMin, srcMax, dstMin, dstMax).clamp(dstMin, dstMax)
/** Converts this value considering it was in the range [srcMin]..[srcMax] into [dstMin]..[dstMax], if the value is not inside the range the output value will be clamped to the nearest bound */
@Deprecated("") public fun Double.convertRangeClamped(srcMin: Double, srcMax: Double, dstMin: Double, dstMax: Double): Double = convertRange(srcMin, srcMax, dstMin, dstMax).clamp(dstMin, dstMax)
/** Converts this value considering it was in the range [srcMin]..[srcMax] into [dstMin]..[dstMax], if the value is not inside the range the output value will be clamped to the nearest bound */
@Deprecated("") public fun Int.convertRangeClamped(srcMin: Int, srcMax: Int, dstMin: Int, dstMax: Int): Int = convertRange(srcMin, srcMax, dstMin, dstMax).clamp(dstMin, dstMax)
/** Converts this value considering it was in the range [srcMin]..[srcMax] into [dstMin]..[dstMax], if the value is not inside the range the output value will be clamped to the nearest bound */
@Deprecated("") public fun Long.convertRangeClamped(srcMin: Long, srcMax: Long, dstMin: Long, dstMax: Long): Long = convertRange(srcMin, srcMax, dstMin, dstMax).clamp(dstMin, dstMax)

////////////////////
////////////////////

/** Check if the absolute value of [this] floating point value is small (abs(this) <= 1e-6) */
@Deprecated("") public fun Float.isAlmostZero(): Boolean = abs(this) <= 1e-6
/** Check if the absolute value of [this] floating point value is small (abs(this) <= 1e-19) */
@Deprecated("") public fun Double.isAlmostZero(): Boolean = abs(this) <= 1e-19

/** Check if [this] floating point value is not a number or infinite */
@Deprecated("") public fun Float.isNanOrInfinite(): Boolean = this.isNaN() || this.isInfinite()
/** Check if [this] floating point value is not a number or infinite */
@Deprecated("") public fun Double.isNanOrInfinite(): Boolean = this.isNaN() || this.isInfinite()

////////////////////
////////////////////

/** Performs the unsigned modulo between [this] and [other] (negative values would wrap) */
@Deprecated("") public infix fun Int.umod(other: Int): Int {
    val rm = this % other
    val remainder = if (rm == -0) 0 else rm
    return when {
        remainder < 0 -> remainder + other
        else -> remainder
    }
}

/** Performs the unsigned modulo between [this] and [other] (negative values would wrap) */
@Deprecated("") public infix fun Double.umod(other: Double): Double {
    val rm = this % other
    val remainder = if (rm == -0.0) 0.0 else rm
    return when {
        remainder < 0.0 -> remainder + other
        else -> remainder
    }
}

private val MINUS_ZERO_F = -0f
@Deprecated("") public infix fun Float.umod(other: Float): Float {
    val rm = this % other
    val remainder = if (rm == MINUS_ZERO_F) 0f else rm
    return when {
        remainder < 0f -> remainder + other
        else -> remainder
    }
}

@Deprecated("") public inline fun fract(value: Float): Float = value - value.toIntFloor()
@Deprecated("") public inline fun fract(value: Double): Double = value - value.toIntFloor()


////////////////////
////////////////////

/** Returns the next value of [this] that is multiple of [align]. If [this] is already multiple, returns itself. */
@Deprecated("") public fun Int.nextAlignedTo(align: Int): Int = if (this.isAlignedTo(align)) this else (((this / align) + 1) * align)
/** Returns the next value of [this] that is multiple of [align]. If [this] is already multiple, returns itself. */
@Deprecated("") public fun Long.nextAlignedTo(align: Long): Long = if (this.isAlignedTo(align)) this else (((this / align) + 1) * align)
/** Returns the next value of [this] that is multiple of [align]. If [this] is already multiple, returns itself. */
@Deprecated("") public fun Float.nextAlignedTo(align: Float): Float = if (this.isAlignedTo(align)) this else (((this / align).toInt() + 1) * align)
/** Returns the next value of [this] that is multiple of [align]. If [this] is already multiple, returns itself. */
@Deprecated("") public fun Double.nextAlignedTo(align: Double): Double = if (this.isAlignedTo(align)) this else (((this / align).toInt() + 1) * align)

/** Returns the previous value of [this] that is multiple of [align]. If [this] is already multiple, returns itself. */
@Deprecated("") public fun Int.prevAlignedTo(align: Int): Int = if (this.isAlignedTo(align)) this else nextAlignedTo(align) - align
/** Returns the previous value of [this] that is multiple of [align]. If [this] is already multiple, returns itself. */
@Deprecated("") public fun Long.prevAlignedTo(align: Long): Long = if (this.isAlignedTo(align)) this else nextAlignedTo(align) - align
/** Returns the previous value of [this] that is multiple of [align]. If [this] is already multiple, returns itself. */
@Deprecated("") public fun Float.prevAlignedTo(align: Float): Float = if (this.isAlignedTo(align)) this else nextAlignedTo(align) - align
/** Returns the previous value of [this] that is multiple of [align]. If [this] is already multiple, returns itself. */
@Deprecated("") public fun Double.prevAlignedTo(align: Double): Double = if (this.isAlignedTo(align)) this else nextAlignedTo(align) - align

/** Returns whether [this] is multiple of [alignment] */
@Deprecated("") public fun Int.isAlignedTo(alignment: Int): Boolean = alignment == 0 || (this % alignment) == 0
/** Returns whether [this] is multiple of [alignment] */
@Deprecated("") public fun Long.isAlignedTo(alignment: Long): Boolean = alignment == 0L || (this % alignment) == 0L
/** Returns whether [this] is multiple of [alignment] */
@Deprecated("") public fun Float.isAlignedTo(alignment: Float): Boolean = alignment == 0f || (this % alignment) == 0f
/** Returns whether [this] is multiple of [alignment] */
@Deprecated("") public fun Double.isAlignedTo(alignment: Double): Boolean = alignment == 0.0 || (this % alignment) == 0.0

/** Returns the previous or next value of [this] that is multiple of [align]. If [this] is already multiple, returns itself. */
@Deprecated("") public fun Float.nearestAlignedTo(align: Float): Float {
    val prev = this.prevAlignedTo(align)
    val next = this.nextAlignedTo(align)
    return if ((this - prev).absoluteValue < (this - next).absoluteValue) prev else next
}
/** Returns the previous or next value of [this] that is multiple of [align]. If [this] is already multiple, returns itself. */
@Deprecated("") public fun Double.nearestAlignedTo(align: Double): Double {
    val prev = this.prevAlignedTo(align)
    val next = this.nextAlignedTo(align)
    return if ((this - prev).absoluteValue < (this - next).absoluteValue) prev else next
}


////////////////////
////////////////////

/** Clamps [this] value into the range [min] and [max] */
@Deprecated("") public fun Int.clamp(min: Int, max: Int): Int = if (this < min) min else if (this > max) max else this
/** Clamps [this] value into the range [min] and [max] */
@Deprecated("") public fun Long.clamp(min: Long, max: Long): Long = if (this < min) min else if (this > max) max else this
/** Clamps [this] value into the range [min] and [max] */
@Deprecated("") public fun Double.clamp(min: Double, max: Double): Double = if (this < min) min else if (this > max) max else this
/** Clamps [this] value into the range [min] and [max] */
@Deprecated("") public fun Float.clamp(min: Float, max: Float): Float = if ((this < min)) min else if ((this > max)) max else this

/** Clamps [this] value into the range 0 and 1 */
@Deprecated("") public fun Double.clamp01(): Double = clamp(0.0, 1.0)
/** Clamps [this] value into the range 0 and 1 */
@Deprecated("") public fun Float.clamp01(): Float = clamp(0f, 1f)

/** Clamps [this] [Long] value into the range [min] and [max] converting it into [Int]. The default parameters will cover the whole range of values. */
@Deprecated("") public fun Long.toIntClamp(min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE): Int {
    if (this < min) return min
    if (this > max) return max
    return this.toInt()
}

/** Clamps [this] [Long] value into the range [min] and [max] converting it into [Int] (where [min] must be zero or positive). The default parameters will cover the whole range of positive and zero values. */
@Deprecated("") public fun Long.toUintClamp(min: Int = 0, max: Int = Int.MAX_VALUE): Int = this.toIntClamp(min, max)

/** Clamps the integer value in the 0..255 range */
@Deprecated("") fun Int.clampUByte(): Int {
    val n = this and -(if (this >= 0) 1 else 0)
    return (n or (0xFF - n shr 31)) and 0xFF
}
@Deprecated("") fun Int.clampUShort(): Int {
    val n = this and -(if (this >= 0) 1 else 0)
    return (n or (0xFFFF - n shr 31)) and 0xFFFF
}

@Deprecated("") fun Int.toShortClamped(): Short = this.clamp(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
@Deprecated("") fun Int.toByteClamped(): Byte = this.clamp(Byte.MIN_VALUE.toInt(), Byte.MAX_VALUE.toInt()).toByte()

////////////////////
////////////////////

/** Checks if [this] is odd (not multiple of two) */
@Deprecated("") public val Int.isOdd: Boolean get() = (this % 2) == 1
/** Checks if [this] is even (multiple of two) */
@Deprecated("") public val Int.isEven: Boolean get() = (this % 2) == 0

////////////////////
////////////////////

/** Returns the next power of two of [this] */
@Deprecated("") public val Int.nextPowerOfTwo: Int get() {
    var v = this
    v--
    v = v or (v shr 1)
    v = v or (v shr 2)
    v = v or (v shr 4)
    v = v or (v shr 8)
    v = v or (v shr 16)
    v++
    return v
}

/** Returns the previous power of two of [this] */
@Deprecated("") public val Int.prevPowerOfTwo: Int get() = if (isPowerOfTwo) this else (nextPowerOfTwo ushr 1)

/** Checks if [this] value is power of two */
@Deprecated("") public val Int.isPowerOfTwo: Boolean get() = this.nextPowerOfTwo == this
*/

package korlibs.time

import korlibs.math.*
import korlibs.number.*
import korlibs.time.internal.*
import kotlin.jvm.*
import kotlin.math.*
import kotlin.time.*
import kotlin.time.Duration.Companion.milliseconds

private val DURATION_NIL = (-0x001FFFFFFFFFFFF3L).toDuration(DurationUnit.NANOSECONDS)

val Duration.Companion.NIL get() = DURATION_NIL
//val Duration.Companion.ZERO get() = Duration.ZERO

/** [TimeSpan] representing this number as [nanoseconds] or 1 / 1_000_000_000 [seconds]. */
inline val Long.nanoseconds get() = toDuration(DurationUnit.NANOSECONDS)
/** [TimeSpan] representing this number as [microseconds] or 1 / 1_000_000 [seconds]. */
inline val Long.microseconds get() = toDuration(DurationUnit.MICROSECONDS)
/** [TimeSpan] representing this number as [milliseconds] or 1 / 1_000 [seconds]. */
inline val Long.milliseconds get() = toDuration(DurationUnit.MILLISECONDS)
/** [TimeSpan] representing this number as [seconds]. */
inline val Long.seconds get() = toDuration(DurationUnit.SECONDS)
/** [TimeSpan] representing this number as [minutes] or 60 [seconds]. */
inline val Long.minutes get() = toDuration(DurationUnit.MINUTES)
/** [TimeSpan] representing this number as [hours] or 3_600 [seconds]. */
inline val Long.hours get() = toDuration(DurationUnit.HOURS)
/** [TimeSpan] representing this number as [days] or 86_400 [seconds]. */
inline val Long.days get() = toDuration(DurationUnit.DAYS)
/** [TimeSpan] representing this number as [weeks] or 604_800 [seconds]. */
inline val Long.weeks get() = (this * 7).days

/** [TimeSpan] representing this number as [nanoseconds] or 1 / 1_000_000_000 [seconds]. */
inline val Float.nanoseconds get() = this.toDouble().nanoseconds
/** [TimeSpan] representing this number as [microseconds] or 1 / 1_000_000 [seconds]. */
inline val Float.microseconds get() = this.toDouble().microseconds
/** [TimeSpan] representing this number as [milliseconds] or 1 / 1_000 [seconds]. */
inline val Float.milliseconds get() = this.toDouble().milliseconds
/** [TimeSpan] representing this number as [seconds]. */
inline val Float.seconds get() = this.toDouble().seconds
/** [TimeSpan] representing this number as [minutes] or 60 [seconds]. */
inline val Float.minutes get() = this.toDouble().minutes
/** [TimeSpan] representing this number as [hours] or 3_600 [seconds]. */
inline val Float.hours get() = this.toDouble().hours
/** [TimeSpan] representing this number as [days] or 86_400 [seconds]. */
inline val Float.days get() = this.toDouble().days
/** [TimeSpan] representing this number as [weeks] or 604_800 [seconds]. */
inline val Float.weeks get() = this.toDouble().weeks

/** [TimeSpan] representing this number as [nanoseconds] or 1 / 1_000_000_000 [seconds]. */
inline val Int.nanoseconds get() = this.toDouble().nanoseconds
/** [TimeSpan] representing this number as [microseconds] or 1 / 1_000_000 [seconds]. */
inline val Int.microseconds get() = this.toDouble().microseconds
/** [TimeSpan] representing this number as [milliseconds] or 1 / 1_000 [seconds]. */
inline val Int.milliseconds get() = this.toDouble().milliseconds
/** [TimeSpan] representing this number as [seconds]. */
inline val Int.seconds get() = this.toDouble().seconds
/** [TimeSpan] representing this number as [minutes] or 60 [seconds]. */
inline val Int.minutes get() = this.toDouble().minutes
/** [TimeSpan] representing this number as [hours] or 3_600 [seconds]. */
inline val Int.hours get() = this.toDouble().hours
/** [TimeSpan] representing this number as [days] or 86_400 [seconds]. */
inline val Int.days get() = this.toDouble().days
/** [TimeSpan] representing this number as [weeks] or 604_800 [seconds]. */
inline val Int.weeks get() = this.toDouble().weeks

/** [TimeSpan] representing this number as [nanoseconds] or 1 / 1_000_000_000 [seconds]. */
inline val Double.nanoseconds get() = toDuration(DurationUnit.NANOSECONDS)
/** [TimeSpan] representing this number as [microseconds] or 1 / 1_000_000 [seconds]. */
inline val Double.microseconds get() = toDuration(DurationUnit.MICROSECONDS)
/** [TimeSpan] representing this number as [milliseconds] or 1 / 1_000 [seconds]. */
inline val Double.milliseconds get() = toDuration(DurationUnit.MILLISECONDS)
/** [TimeSpan] representing this number as [seconds]. */
inline val Double.seconds get() = toDuration(DurationUnit.SECONDS)
/** [TimeSpan] representing this number as [minutes] or 60 [seconds]. */
inline val Double.minutes get() = toDuration(DurationUnit.MINUTES)
/** [TimeSpan] representing this number as [hours] or 3_600 [seconds]. */
inline val Double.hours get() = toDuration(DurationUnit.HOURS)
/** [TimeSpan] representing this number as [days] or 86_400 [seconds]. */
inline val Double.days get() = toDuration(DurationUnit.DAYS)
/** [TimeSpan] representing this number as [weeks] or 604_800 [seconds]. */
inline val Double.weeks get() = (this * 7).days


/**
 * Represents a span of time, with [milliseconds] precision.
 *
 * It is a value class wrapping [Double] instead of [Long] to work on JavaScript without allocations.
 */
typealias TimeSpan = Duration

operator fun Duration.unaryPlus(): Duration = this

val Duration.milliseconds: Double get() = this.inWholeMilliseconds.toDouble()

/** Returns the total number of [nanoseconds] for this [TimeSpan] (1 / 1_000_000_000 [seconds]) */
val Duration.nanoseconds: Double get() = this.milliseconds / MILLIS_PER_NANOSECOND
/** Returns the total number of [nanoseconds] for this [TimeSpan] (1 / 1_000_000_000 [seconds]) as Integer */
val Duration.nanosecondsInt: Int get() = (this.milliseconds / MILLIS_PER_NANOSECOND).toInt()

/** Returns the total number of [microseconds] for this [TimeSpan] (1 / 1_000_000 [seconds]) */
val Duration.microseconds: Double get() = this.milliseconds / MILLIS_PER_MICROSECOND
/** Returns the total number of [microseconds] for this [TimeSpan] (1 / 1_000_000 [seconds]) as Integer */
val Duration.microsecondsInt: Int get() = (this.milliseconds / MILLIS_PER_MICROSECOND).toInt()

/** Returns the total number of [seconds] for this [TimeSpan] */
val Duration.seconds: Double get() = this.milliseconds / MILLIS_PER_SECOND
/** Returns the total number of [minutes] for this [TimeSpan] (60 [seconds]) */
val Duration.minutes: Double get() = this.milliseconds / MILLIS_PER_MINUTE
/** Returns the total number of [hours] for this [TimeSpan] (3_600 [seconds]) */
val Duration.hours: Double get() = this.milliseconds / MILLIS_PER_HOUR
/** Returns the total number of [days] for this [TimeSpan] (86_400 [seconds]) */
val Duration.days: Double get() = this.milliseconds / MILLIS_PER_DAY
/** Returns the total number of [weeks] for this [TimeSpan] (604_800 [seconds]) */
val Duration.weeks: Double get() = this.milliseconds / MILLIS_PER_WEEK

/** Returns the total number of [milliseconds] as a [Long] */
val Duration.millisecondsLong: Long get() = milliseconds.toLong()
/** Returns the total number of [milliseconds] as an [Int] */
val Duration.millisecondsInt: Int get() = milliseconds.toInt()

fun TimeSpan(milliseconds: Double): Duration = milliseconds.milliseconds

operator fun Duration.plus(other: MonthSpan): DateTimeSpan = DateTimeSpan(other, this)
operator fun Duration.plus(other: DateTimeSpan): DateTimeSpan = DateTimeSpan(other.monthSpan, other.timeSpan + this)

operator fun Duration.minus(other: MonthSpan): DateTimeSpan = this + (-other)
operator fun Duration.minus(other: DateTimeSpan): DateTimeSpan = this + (-other)

operator fun Duration.times(scale: Float): Duration = TimeSpan((this.milliseconds * scale))
operator fun Duration.div(scale: Float): Duration = TimeSpan(this.milliseconds / scale)

infix fun Duration.divFloat(other: Duration): Float = (this.milliseconds / other.milliseconds).toFloat()
operator fun Duration.rem(other: Duration): Duration = (this.milliseconds % other.milliseconds).milliseconds
infix fun Duration.umod(other: Duration): Duration = (this.milliseconds umod other.milliseconds).milliseconds

/** Return true if [TimeSpan.NIL] */
val Duration.isNil: Boolean get() = this == DURATION_NIL

/**
 * Formats this [TimeSpan] into something like `12:30:40.100`.
 *
 * For 3 hour, 20 minutes and 15 seconds
 *
 * 1 [components] (seconds): 12015
 * 2 [components] (minutes): 200:15
 * 3 [components] (hours)  : 03:20:15
 * 4 [components] (days)   : 00:03:20:15
 *
 * With milliseconds would add decimals to the seconds part.
 */
fun Duration.toTimeString(components: Int = 3, addMilliseconds: Boolean = false): String =
    toTimeString(milliseconds, components, addMilliseconds)

fun Duration.roundMilliseconds(): Duration = kotlin.math.round(milliseconds).milliseconds
fun max(a: Duration, b: Duration): Duration = max(a.milliseconds, b.milliseconds).milliseconds
fun min(a: Duration, b: Duration): Duration = min(a.milliseconds, b.milliseconds).milliseconds
fun Duration.clamp(min: Duration, max: Duration): Duration = when {
    this < min -> min
    this > max -> max
    else -> this
}
inline fun Duration.coalesce(block: () -> Duration): Duration = if (this != Duration.NIL) this else block()

private val timeSteps = listOf(60, 60, 24)
private fun toTimeStringRaw(totalMilliseconds: Double, components: Int = 3): String {
    var timeUnit = floor(totalMilliseconds / 1000.0).toInt()

    val out = arrayListOf<String>()

    for (n in 0 until components) {
        if (n == components - 1) {
            out += timeUnit.padded(2)
            break
        }
        val step = timeSteps.getOrNull(n) ?: throw RuntimeException("Just supported ${timeSteps.size} steps")
        val cunit = timeUnit % step
        timeUnit /= step
        out += cunit.padded(2)
    }

    return out.reversed().joinToString(":")
}

@PublishedApi
internal fun toTimeString(totalMilliseconds: Double, components: Int = 3, addMilliseconds: Boolean = false): String {
    val milliseconds = (totalMilliseconds % 1000).toInt()
    val out = toTimeStringRaw(totalMilliseconds, components)
    return if (addMilliseconds) "$out.$milliseconds" else out
}

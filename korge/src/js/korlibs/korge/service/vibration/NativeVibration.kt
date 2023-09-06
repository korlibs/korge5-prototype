package korlibs.korge.service.vibration

import korlibs.korge.view.*
import korlibs.platform.*
import korlibs.time.*

actual class NativeVibration actual constructor(val views: Views) {

    /**
     * @param timings list of alternating ON-OFF durations in milliseconds. Staring with ON.
     * @param amplitudes has no effect on JS backend
     */
    @ExperimentalUnsignedTypes
    actual fun vibratePattern(timings: Array<TimeSpan>, amplitudes: Array<Double>) {
        jsWindow.navigator.vibrate(timings.map { it.milliseconds }.toTypedArray())
    }

    /**
     * @param time vibration duration in milliseconds
     * @param amplitude has no effect on JS backend
     */
    @ExperimentalUnsignedTypes
    actual fun vibrate(time: TimeSpan, amplitude: Double) {
        jsWindow.navigator.vibrate(time.milliseconds)
    }
}

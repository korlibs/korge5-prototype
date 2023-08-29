package korlibs.audio.sound

import korlibs.memory.clamp

object SampleConvert {
    // (value.clamp(-1f, +1f) * Short.MAX_VALUE).toShort()
    fun floatToShort(v: Float): Short = (v * Short.MAX_VALUE.toDouble()).toInt().clamp(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    fun shortToFloat(v: Short): Float = (v.toDouble() / Short.MAX_VALUE).toFloat()

    fun convertS16ToF32(channels: Int, input: ShortArray, leftVolume: Float, rightVolume: Float): FloatArray {
        val output = FloatArray(input.size * 2 / channels)
        val optimized = leftVolume == 1f && rightVolume == 1f
        when (channels) {
            2 ->
                if (optimized) {
                    for (n in 0 until output.size) output[n] = (input[n] / 32767f)
                } else {
                    for (n in 0 until output.size step 2) {
                        output[n + 0] = ((input[n + 0] / 32767f) * leftVolume)
                        output[n + 1] = ((input[n + 1] / 32767f) * rightVolume)
                    }
                }
            1 ->
                if (optimized) {
                    var m = 0
                    for (n in 0 until input.size) {
                        val v = (input[n] / 32767f)
                        output[m++] = v
                        output[m++] = v
                    }
                } else {
                    var m = 0
                    for (n in 0 until input.size) {
                        val sample = (input[n] / 32767f)
                        output[m++] = sample * leftVolume
                        output[m++] = sample * rightVolume
                    }
                }
        }
        return output
    }
}

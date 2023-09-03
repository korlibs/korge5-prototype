package korlibs.korge.render

import korlibs.image.bitmap.*
import korlibs.math.*

/**
 * Returns [this] same bitmap if it is already power of two,
 * or creates a new [Bitmap32] with the pixels from [this] set at the top-left while keeping the rest of the [Bitmap32] with transparent pixels.
 */
fun Bitmap32.ensurePowerOfTwo(): Bitmap32 = when {
    this.width.isPowerOfTwo && this.height.isPowerOfTwo -> this
    else -> Bitmap32(this.width.nextPowerOfTwo, this.height.nextPowerOfTwo, this.premultiplied).also { it.put(this) }
}

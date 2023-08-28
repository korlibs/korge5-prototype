package korlibs.korge

import korlibs.math.geom.Anchor
import korlibs.math.geom.ScaleMode

data class KorgeDisplayMode(val scaleMode: ScaleMode, val scaleAnchor: Anchor, val clipBorders: Boolean) {
    companion object {
        val DEFAULT get() = CENTER
        val CENTER = KorgeDisplayMode(ScaleMode.SHOW_ALL, Anchor.CENTER, clipBorders = true)
        //@Deprecated("Typically TOP_LEFT_NO_CLIP is better")
        val CENTER_NO_CLIP = KorgeDisplayMode(ScaleMode.SHOW_ALL, Anchor.CENTER, clipBorders = false)
        val TOP_LEFT_NO_CLIP = KorgeDisplayMode(ScaleMode.SHOW_ALL, Anchor.TOP_LEFT, clipBorders = false)
        val NO_SCALE = KorgeDisplayMode(ScaleMode.NO_SCALE, Anchor.TOP_LEFT, clipBorders = false)
    }
}

package korlibs.template

import korlibs.io.util.*

class AutoEscapeMode(val transform: (String) -> String) {
    companion object {
        val HTML = AutoEscapeMode { it.htmlspecialchars() }
        val RAW = AutoEscapeMode { it }
    }
}

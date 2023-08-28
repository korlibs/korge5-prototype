package korlibs.render.ffi.sdl

import korlibs.event.Key
import korlibs.ffi.FFILib
import korlibs.ffi.FFIPointer

// https://gist.github.com/m1nuz/5c48ddd6a3fd8fb340c037165edde1fb
object SDL : FFILib(
    "SDL2",
    //"/Library/Frameworks/SDL2.framework/SDL2",
    //"/opt/homebrew/Cellar/sdl2/2.28.1/lib/libSDL2.dylib",
    //"libSDL2",
    //"/usr/lib/aarch64-linux-gnu/libSDL2-2.0.so",
    //"SDL2.dll",
) {
    val SDL_GetWindowSize: (FFIPointer?, IntArray, IntArray) -> Unit by func()
    val SDL_SetWindowSize: (window: FFIPointer?, width: Int, height: Int) -> Unit by func()
    val SDL_SetWindowPosition: (FFIPointer?, Int, Int) -> Unit by func()
    val SDL_SetWindowTitle: (FFIPointer?, ByteArray) -> Unit by func()
    val SDL_GL_CreateContext: (FFIPointer?) -> FFIPointer? by func()
    val SDL_GL_DeleteContext: (FFIPointer?) -> Unit by func()
    val SDL_GL_SwapWindow: (FFIPointer?) -> Unit by func()
    val SDL_GL_MakeCurrent: (FFIPointer?, FFIPointer?) -> Int by func()
    val SDL_InitSubSystem: (Int) -> Int by func()
    val SDL_QuitSubSystem: (Int) -> Int by func()
    val SDL_CreateWindow: (String?, Int, Int, Int, Int, Int) -> FFIPointer? by func()
    val SDL_DestroyWindow: (FFIPointer?) -> Unit by func()
    val SDL_GetError: () -> FFIPointer? by func()
    val SDL_Delay: (Int) -> Int by func()
    val SDL_PumpEvents: () -> Unit by func()
    val SDL_PollEvent: (IntArray) -> Boolean by func()
    val SDL_WaitEventTimeout: (IntArray, Int) -> Int by func()
    val SDL_GL_LoadLibrary: (String?) -> Int by func()
    val SDL_GL_UnloadLibrary: () -> Unit by func()
    val SDL_GL_GetProcAddress: (String) -> FFIPointer? by func()
    val SDL_GL_SetSwapInterval: (Int) -> Int by func()
    val SDL_GL_SetAttribute: (Int, Int) -> Int by func()

    fun SDL_CreateOpenGLWindow(width: Int, height: Int, title: String? = null, shown: Boolean = true): FFIPointer? {
        val windowFlags = SDL_WINDOW_OPENGL or SDL_WINDOW_RESIZABLE or(if (shown) SDL_WINDOW_SHOWN else SDL_WINDOW_HIDDEN)
        val contextFlags = SDL_GL_CONTEXT_FORWARD_COMPATIBLE_FLAG //or (gl_debug ? SDL_GL_CONTEXT_DEBUG_FLAG : 0);
        SDL.SDL_GL_SetAttribute(SDL_GL_CONTEXT_MAJOR_VERSION, 3)
        SDL.SDL_GL_SetAttribute(SDL_GL_CONTEXT_MINOR_VERSION, 1)
        SDL.SDL_GL_SetAttribute(SDL_GL_CONTEXT_PROFILE_MASK, SDL_GL_CONTEXT_PROFILE_CORE)
        SDL.SDL_GL_SetAttribute(SDL_GL_CONTEXT_FLAGS, contextFlags)
        SDL.SDL_GL_SetAttribute(SDL_GL_DOUBLEBUFFER, 1)
        return SDL.SDL_CreateWindow(title, SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED, width, height, windowFlags)
    }
}

const val SDL_WINDOWPOS_CENTERED = 0x2FFF0000

const val SDL_QUIT = 0x100
const val SDL_KEYDOWN = 0x300
const val SDL_KEYUP = 0x301

const val SDL_WINDOWEVENT  = 0x200 /**< Window state change */

const val SDL_MOUSEMOTION    = 0x400 /**< Mouse moved */
const val SDL_MOUSEBUTTONDOWN = 0x401
const val SDL_MOUSEBUTTONUP = 0x0402          /**< Mouse button released */
const val SDL_MOUSEWHEEL = 0x403

const val GL_DEPTH_BUFFER_BIT = 0x00000100
const val GL_STENCIL_BUFFER_BIT = 0x00000400
const val GL_COLOR_BUFFER_BIT = 0x00004000

const val SDL_WINDOW_FULLSCREEN = 0x00000001

/**< fullscreen window */
const val SDL_WINDOW_OPENGL = 0x00000002

/**< window usable with OpenGL context */
const val SDL_WINDOW_SHOWN = 0x00000004

/**< window is visible */
const val SDL_WINDOW_HIDDEN = 0x00000008

/**< window is not visible */
const val SDL_WINDOW_BORDERLESS = 0x00000010

/**< no window decoration */
const val SDL_WINDOW_RESIZABLE = 0x00000020

/**< window can be resized */
const val SDL_WINDOW_MINIMIZED = 0x00000040

/**< window is minimized */
const val SDL_WINDOW_MAXIMIZED = 0x00000080

/**< window is maximized */
const val SDL_WINDOW_MOUSE_GRABBED = 0x00000100

/**< window has grabbed mouse input */
const val SDL_WINDOW_INPUT_FOCUS = 0x00000200

/**< window has input focus */
const val SDL_WINDOW_MOUSE_FOCUS = 0x00000400

/**< window has mouse focus */
const val SDL_WINDOW_FULLSCREEN_DESKTOP = (SDL_WINDOW_FULLSCREEN or 0x00001000)
const val SDL_WINDOW_FOREIGN = 0x00000800

/**< window not created by SDL */
const val SDL_WINDOW_ALLOW_HIGHDPI = 0x00002000

/**< window should be created in high-DPI mode if supported.
On macOS NSHighResolutionCapable must be set true in the
application's Info.plist for this to have any effect. */
const val SDL_WINDOW_MOUSE_CAPTURE = 0x00004000

/**< window has mouse captured (unrelated to MOUSE_GRABBED) */
const val SDL_WINDOW_ALWAYS_ON_TOP = 0x00008000

/**< window should always be above others */
const val SDL_WINDOW_SKIP_TASKBAR = 0x00010000

/**< window should not be added to the taskbar */
const val SDL_WINDOW_UTILITY = 0x00020000

/**< window should be treated as a utility window */
const val SDL_WINDOW_TOOLTIP = 0x00040000

/**< window should be treated as a tooltip */
const val SDL_WINDOW_POPUP_MENU = 0x00080000

/**< window should be treated as a popup menu */
const val SDL_WINDOW_KEYBOARD_GRABBED = 0x00100000

/**< window has grabbed keyboard input */
const val SDL_WINDOW_VULKAN = 0x10000000

/**< window usable for Vulkan surface */
const val SDL_WINDOW_METAL = 0x20000000
/**< window usable for Metal view */


/**< Mouse button pressed */
const val SDL_GL_CONTEXT_DEBUG_FLAG              = 0x0001
const val SDL_GL_CONTEXT_FORWARD_COMPATIBLE_FLAG = 0x0002
const val SDL_GL_CONTEXT_ROBUST_ACCESS_FLAG      = 0x0004
const val SDL_GL_CONTEXT_RESET_ISOLATION_FLAG    = 0x0008

const val SDL_GL_RED_SIZE = 0
const val SDL_GL_GREEN_SIZE = 1
const val SDL_GL_BLUE_SIZE = 2
const val SDL_GL_ALPHA_SIZE = 3
const val SDL_GL_BUFFER_SIZE = 4
const val SDL_GL_DOUBLEBUFFER = 5
const val SDL_GL_DEPTH_SIZE = 6
const val SDL_GL_STENCIL_SIZE = 7
const val SDL_GL_ACCUM_RED_SIZE = 8
const val SDL_GL_ACCUM_GREEN_SIZE = 9
const val SDL_GL_ACCUM_BLUE_SIZE = 10
const val SDL_GL_ACCUM_ALPHA_SIZE = 11
const val SDL_GL_STEREO = 12
const val SDL_GL_MULTISAMPLEBUFFERS = 13
const val SDL_GL_MULTISAMPLESAMPLES = 14
const val SDL_GL_ACCELERATED_VISUAL = 15
const val SDL_GL_RETAINED_BACKING = 16
const val SDL_GL_CONTEXT_MAJOR_VERSION = 17
const val SDL_GL_CONTEXT_MINOR_VERSION = 18
const val SDL_GL_CONTEXT_FLAGS = 19
const val SDL_GL_CONTEXT_PROFILE_MASK = 20
const val SDL_GL_SHARE_WITH_CURRENT_CONTEXT = 21
const val SDL_GL_FRAMEBUFFER_SRGB_CAPABLE = 22
const val SDL_GL_CONTEXT_RELEASE_BEHAVIOR = 23
const val SDL_GL_CONTEXT_RESET_NOTIFICATION = 24
const val SDL_GL_CONTEXT_NO_ERROR = 25
const val SDL_GL_FLOATBUFFERS = 26
const val SDL_GL_EGL_PLATFORM = 27

const val SDL_GL_CONTEXT_PROFILE_CORE           = 0x0001
const val SDL_GL_CONTEXT_PROFILE_COMPATIBILITY  = 0x0002
const val SDL_GL_CONTEXT_PROFILE_ES             = 0x0004 /**< GLX_CONTEXT_ES2_PROFILE_BIT_EXT */

object SDL_SCANCODES {
    const val SDL_SCANCODE_UNKNOWN = 0

    const val SDL_SCANCODE_A = 4
    const val SDL_SCANCODE_B = 5
    const val SDL_SCANCODE_C = 6
    const val SDL_SCANCODE_D = 7
    const val SDL_SCANCODE_E = 8
    const val SDL_SCANCODE_F = 9
    const val SDL_SCANCODE_G = 10
    const val SDL_SCANCODE_H = 11
    const val SDL_SCANCODE_I = 12
    const val SDL_SCANCODE_J = 13
    const val SDL_SCANCODE_K = 14
    const val SDL_SCANCODE_L = 15
    const val SDL_SCANCODE_M = 16
    const val SDL_SCANCODE_N = 17
    const val SDL_SCANCODE_O = 18
    const val SDL_SCANCODE_P = 19
    const val SDL_SCANCODE_Q = 20
    const val SDL_SCANCODE_R = 21
    const val SDL_SCANCODE_S = 22
    const val SDL_SCANCODE_T = 23
    const val SDL_SCANCODE_U = 24
    const val SDL_SCANCODE_V = 25
    const val SDL_SCANCODE_W = 26
    const val SDL_SCANCODE_X = 27
    const val SDL_SCANCODE_Y = 28
    const val SDL_SCANCODE_Z = 29

    const val SDL_SCANCODE_1 = 30
    const val SDL_SCANCODE_2 = 31
    const val SDL_SCANCODE_3 = 32
    const val SDL_SCANCODE_4 = 33
    const val SDL_SCANCODE_5 = 34
    const val SDL_SCANCODE_6 = 35
    const val SDL_SCANCODE_7 = 36
    const val SDL_SCANCODE_8 = 37
    const val SDL_SCANCODE_9 = 38
    const val SDL_SCANCODE_0 = 39

    const val SDL_SCANCODE_RETURN = 40
    const val SDL_SCANCODE_ESCAPE = 41
    const val SDL_SCANCODE_BACKSPACE = 42
    const val SDL_SCANCODE_TAB = 43
    const val SDL_SCANCODE_SPACE = 44

    const val SDL_SCANCODE_MINUS = 45
    const val SDL_SCANCODE_EQUALS = 46
    const val SDL_SCANCODE_LEFTBRACKET = 47
    const val SDL_SCANCODE_RIGHTBRACKET = 48
    const val SDL_SCANCODE_BACKSLASH = 49
    const val SDL_SCANCODE_NONUSHASH = 50
    const val SDL_SCANCODE_SEMICOLON = 51
    const val SDL_SCANCODE_APOSTROPHE = 52
    const val SDL_SCANCODE_GRAVE = 53
    const val SDL_SCANCODE_COMMA = 54
    const val SDL_SCANCODE_PERIOD = 55
    const val SDL_SCANCODE_SLASH = 56

    const val SDL_SCANCODE_CAPSLOCK = 57

    const val SDL_SCANCODE_F1 = 58
    const val SDL_SCANCODE_F2 = 59
    const val SDL_SCANCODE_F3 = 60
    const val SDL_SCANCODE_F4 = 61
    const val SDL_SCANCODE_F5 = 62
    const val SDL_SCANCODE_F6 = 63
    const val SDL_SCANCODE_F7 = 64
    const val SDL_SCANCODE_F8 = 65
    const val SDL_SCANCODE_F9 = 66
    const val SDL_SCANCODE_F10 = 67
    const val SDL_SCANCODE_F11 = 68
    const val SDL_SCANCODE_F12 = 69

    const val SDL_SCANCODE_PRINTSCREEN = 70
    const val SDL_SCANCODE_SCROLLLOCK = 71
    const val SDL_SCANCODE_PAUSE = 72
    const val SDL_SCANCODE_INSERT = 73
    const val SDL_SCANCODE_HOME = 74
    const val SDL_SCANCODE_PAGEUP = 75
    const val SDL_SCANCODE_DELETE = 76
    const val SDL_SCANCODE_END = 77
    const val SDL_SCANCODE_PAGEDOWN = 78
    const val SDL_SCANCODE_RIGHT = 79
    const val SDL_SCANCODE_LEFT = 80
    const val SDL_SCANCODE_DOWN = 81
    const val SDL_SCANCODE_UP = 82

    const val SDL_SCANCODE_NUMLOCKCLEAR = 83
    const val SDL_SCANCODE_KP_DIVIDE = 84
    const val SDL_SCANCODE_KP_MULTIPLY = 85
    const val SDL_SCANCODE_KP_MINUS = 86
    const val SDL_SCANCODE_KP_PLUS = 87
    const val SDL_SCANCODE_KP_ENTER = 88
    const val SDL_SCANCODE_KP_1 = 89
    const val SDL_SCANCODE_KP_2 = 90
    const val SDL_SCANCODE_KP_3 = 91
    const val SDL_SCANCODE_KP_4 = 92
    const val SDL_SCANCODE_KP_5 = 93
    const val SDL_SCANCODE_KP_6 = 94
    const val SDL_SCANCODE_KP_7 = 95
    const val SDL_SCANCODE_KP_8 = 96
    const val SDL_SCANCODE_KP_9 = 97
    const val SDL_SCANCODE_KP_0 = 98
    const val SDL_SCANCODE_KP_PERIOD = 99

    const val SDL_SCANCODE_NONUSBACKSLASH = 100
    const val SDL_SCANCODE_APPLICATION = 101
    const val SDL_SCANCODE_POWER = 102
    const val SDL_SCANCODE_KP_EQUALS = 103
    const val SDL_SCANCODE_F13 = 104
    const val SDL_SCANCODE_F14 = 105
    const val SDL_SCANCODE_F15 = 106
    const val SDL_SCANCODE_F16 = 107
    const val SDL_SCANCODE_F17 = 108
    const val SDL_SCANCODE_F18 = 109
    const val SDL_SCANCODE_F19 = 110
    const val SDL_SCANCODE_F20 = 111
    const val SDL_SCANCODE_F21 = 112
    const val SDL_SCANCODE_F22 = 113
    const val SDL_SCANCODE_F23 = 114
    const val SDL_SCANCODE_F24 = 115
    const val SDL_SCANCODE_EXECUTE = 116
    const val SDL_SCANCODE_HELP = 117

    /**< AL Integrated Help Center */
    const val SDL_SCANCODE_MENU = 118

    /**< Menu (show menu) */
    const val SDL_SCANCODE_SELECT = 119
    const val SDL_SCANCODE_STOP = 120

    /**< AC Stop */
    const val SDL_SCANCODE_AGAIN = 121

    /**< AC Redo/Repeat */
    const val SDL_SCANCODE_UNDO = 122

    /**< AC Undo */
    const val SDL_SCANCODE_CUT = 123

    /**< AC Cut */
    const val SDL_SCANCODE_COPY = 124

    /**< AC Copy */
    const val SDL_SCANCODE_PASTE = 125

    /**< AC Paste */
    const val SDL_SCANCODE_FIND = 126

    /**< AC Find */
    const val SDL_SCANCODE_MUTE = 127
    const val SDL_SCANCODE_VOLUMEUP = 128
    const val SDL_SCANCODE_VOLUMEDOWN = 129

    //const val /*     SDL_SCANCODE_LOCKINGCAPSLOCK = 130  */
    //const val /*     SDL_SCANCODE_LOCKINGNUMLOCK = 131 */
    //const val /*     SDL_SCANCODE_LOCKINGSCROLLLOCK = 132 */
    const val SDL_SCANCODE_KP_COMMA = 133
    const val SDL_SCANCODE_KP_EQUALSAS400 = 134

    const val SDL_SCANCODE_INTERNATIONAL1 = 135
    const val SDL_SCANCODE_INTERNATIONAL2 = 136
    const val SDL_SCANCODE_INTERNATIONAL3 = 137

    /**< Yen */
    const val SDL_SCANCODE_INTERNATIONAL4 = 138
    const val SDL_SCANCODE_INTERNATIONAL5 = 139
    const val SDL_SCANCODE_INTERNATIONAL6 = 140
    const val SDL_SCANCODE_INTERNATIONAL7 = 141
    const val SDL_SCANCODE_INTERNATIONAL8 = 142
    const val SDL_SCANCODE_INTERNATIONAL9 = 143
    const val SDL_SCANCODE_LANG1 = 144

    /**< Hangul/English toggle */
    const val SDL_SCANCODE_LANG2 = 145

    /**< Hanja conversion */
    const val SDL_SCANCODE_LANG3 = 146

    /**< Katakana */
    const val SDL_SCANCODE_LANG4 = 147

    /**< Hiragana */
    const val SDL_SCANCODE_LANG5 = 148

    /**< Zenkaku/Hankaku */
    const val SDL_SCANCODE_LANG6 = 149

    /**< reserved */
    const val SDL_SCANCODE_LANG7 = 150

    /**< reserved */
    const val SDL_SCANCODE_LANG8 = 151

    /**< reserved */
    const val SDL_SCANCODE_LANG9 = 152

    /**< reserved */

    const val SDL_SCANCODE_ALTERASE = 153

    /**< Erase-Eaze */
    const val SDL_SCANCODE_SYSREQ = 154
    const val SDL_SCANCODE_CANCEL = 155

    /**< AC Cancel */
    const val SDL_SCANCODE_CLEAR = 156
    const val SDL_SCANCODE_PRIOR = 157
    const val SDL_SCANCODE_RETURN2 = 158
    const val SDL_SCANCODE_SEPARATOR = 159
    const val SDL_SCANCODE_OUT = 160
    const val SDL_SCANCODE_OPER = 161
    const val SDL_SCANCODE_CLEARAGAIN = 162
    const val SDL_SCANCODE_CRSEL = 163
    const val SDL_SCANCODE_EXSEL = 164

    const val SDL_SCANCODE_KP_00 = 176
    const val SDL_SCANCODE_KP_000 = 177
    const val SDL_SCANCODE_THOUSANDSSEPARATOR = 178
    const val SDL_SCANCODE_DECIMALSEPARATOR = 179
    const val SDL_SCANCODE_CURRENCYUNIT = 180
    const val SDL_SCANCODE_CURRENCYSUBUNIT = 181
    const val SDL_SCANCODE_KP_LEFTPAREN = 182
    const val SDL_SCANCODE_KP_RIGHTPAREN = 183
    const val SDL_SCANCODE_KP_LEFTBRACE = 184
    const val SDL_SCANCODE_KP_RIGHTBRACE = 185
    const val SDL_SCANCODE_KP_TAB = 186
    const val SDL_SCANCODE_KP_BACKSPACE = 187
    const val SDL_SCANCODE_KP_A = 188
    const val SDL_SCANCODE_KP_B = 189
    const val SDL_SCANCODE_KP_C = 190
    const val SDL_SCANCODE_KP_D = 191
    const val SDL_SCANCODE_KP_E = 192
    const val SDL_SCANCODE_KP_F = 193
    const val SDL_SCANCODE_KP_XOR = 194
    const val SDL_SCANCODE_KP_POWER = 195
    const val SDL_SCANCODE_KP_PERCENT = 196
    const val SDL_SCANCODE_KP_LESS = 197
    const val SDL_SCANCODE_KP_GREATER = 198
    const val SDL_SCANCODE_KP_AMPERSAND = 199
    const val SDL_SCANCODE_KP_DBLAMPERSAND = 200
    const val SDL_SCANCODE_KP_VERTICALBAR = 201
    const val SDL_SCANCODE_KP_DBLVERTICALBAR = 202
    const val SDL_SCANCODE_KP_COLON = 203
    const val SDL_SCANCODE_KP_HASH = 204
    const val SDL_SCANCODE_KP_SPACE = 205
    const val SDL_SCANCODE_KP_AT = 206
    const val SDL_SCANCODE_KP_EXCLAM = 207
    const val SDL_SCANCODE_KP_MEMSTORE = 208
    const val SDL_SCANCODE_KP_MEMRECALL = 209
    const val SDL_SCANCODE_KP_MEMCLEAR = 210
    const val SDL_SCANCODE_KP_MEMADD = 211
    const val SDL_SCANCODE_KP_MEMSUBTRACT = 212
    const val SDL_SCANCODE_KP_MEMMULTIPLY = 213
    const val SDL_SCANCODE_KP_MEMDIVIDE = 214
    const val SDL_SCANCODE_KP_PLUSMINUS = 215
    const val SDL_SCANCODE_KP_CLEAR = 216
    const val SDL_SCANCODE_KP_CLEARENTRY = 217
    const val SDL_SCANCODE_KP_BINARY = 218
    const val SDL_SCANCODE_KP_OCTAL = 219
    const val SDL_SCANCODE_KP_DECIMAL = 220
    const val SDL_SCANCODE_KP_HEXADECIMAL = 221

    const val SDL_SCANCODE_LCTRL = 224
    const val SDL_SCANCODE_LSHIFT = 225
    const val SDL_SCANCODE_LALT = 226

    /**< alt option */
    const val SDL_SCANCODE_LGUI = 227

    /**< windows command (apple) meta */
    const val SDL_SCANCODE_RCTRL = 228
    const val SDL_SCANCODE_RSHIFT = 229
    const val SDL_SCANCODE_RALT = 230

    /**< alt gr option */
    const val SDL_SCANCODE_RGUI = 231

    /**< windows command (apple) meta */

    const val SDL_SCANCODE_MODE = 257

    const val SDL_SCANCODE_AUDIONEXT = 258
    const val SDL_SCANCODE_AUDIOPREV = 259
    const val SDL_SCANCODE_AUDIOSTOP = 260
    const val SDL_SCANCODE_AUDIOPLAY = 261
    const val SDL_SCANCODE_AUDIOMUTE = 262
    const val SDL_SCANCODE_MEDIASELECT = 263
    const val SDL_SCANCODE_WWW = 264

    /**< AL Internet Browser */
    const val SDL_SCANCODE_MAIL = 265
    const val SDL_SCANCODE_CALCULATOR = 266

    /**< AL Calculator */
    const val SDL_SCANCODE_COMPUTER = 267
    const val SDL_SCANCODE_AC_SEARCH = 268

    /**< AC Search */
    const val SDL_SCANCODE_AC_HOME = 269

    /**< AC Home */
    const val SDL_SCANCODE_AC_BACK = 270

    /**< AC Back */
    const val SDL_SCANCODE_AC_FORWARD = 271

    /**< AC Forward */
    const val SDL_SCANCODE_AC_STOP = 272

    /**< AC Stop */
    const val SDL_SCANCODE_AC_REFRESH = 273

    /**< AC Refresh */
    const val SDL_SCANCODE_AC_BOOKMARKS = 274

    /**< AC Bookmarks */

    const val SDL_SCANCODE_BRIGHTNESSDOWN = 275
    const val SDL_SCANCODE_BRIGHTNESSUP = 276
    const val SDL_SCANCODE_DISPLAYSWITCH = 277

    /**< display mirroring/dual display
    const val switch video mode switch */
    const val SDL_SCANCODE_KBDILLUMTOGGLE = 278
    const val SDL_SCANCODE_KBDILLUMDOWN = 279
    const val SDL_SCANCODE_KBDILLUMUP = 280
    const val SDL_SCANCODE_EJECT = 281
    const val SDL_SCANCODE_SLEEP = 282

    /**< SC System Sleep */

    const val SDL_SCANCODE_APP1 = 283
    const val SDL_SCANCODE_APP2 = 284

    const val SDL_SCANCODE_AUDIOREWIND = 285
    const val SDL_SCANCODE_AUDIOFASTFORWARD = 286

    const val SDL_SCANCODE_SOFTLEFT = 287

    /**< Usually situated below the display on phones and
    const val used as a multi-function feature key for selecting
    const val a software defined function shown on the bottom left
    const val of the display. */
    const val SDL_SCANCODE_SOFTRIGHT = 288

    /**< Usually situated below the display on phones and
    const val used as a multi-function feature key for selecting
    const val a software defined function shown on the bottom right
    const val of the display. */
    const val SDL_SCANCODE_CALL = 289

    /**< Used for accepting phone calls. */
    const val SDL_SCANCODE_ENDCALL = 290

    /**< Used for rejecting phone calls. */

    const val SDL_NUM_SCANCODES = 512

    /**< not a key just marks the number of scancodes
    const val for array bounds */

    val MAP = mapOf(
        SDL_SCANCODE_UP to Key.UP,
        SDL_SCANCODE_DOWN to Key.DOWN,
        SDL_SCANCODE_RIGHT to Key.RIGHT,
        SDL_SCANCODE_LEFT to Key.LEFT,

        SDL_SCANCODE_A to Key.A,
        SDL_SCANCODE_B to Key.B,
        SDL_SCANCODE_C to Key.C,
        SDL_SCANCODE_D to Key.D,
        SDL_SCANCODE_E to Key.E,
        SDL_SCANCODE_F to Key.F,
        SDL_SCANCODE_G to Key.G,
        SDL_SCANCODE_H to Key.H,
        SDL_SCANCODE_I to Key.I,
        SDL_SCANCODE_J to Key.J,
        SDL_SCANCODE_K to Key.K,
        SDL_SCANCODE_L to Key.L,
        SDL_SCANCODE_M to Key.M,
        SDL_SCANCODE_N to Key.N,
        SDL_SCANCODE_O to Key.O,
        SDL_SCANCODE_P to Key.P,
        SDL_SCANCODE_Q to Key.Q,
        SDL_SCANCODE_R to Key.R,
        SDL_SCANCODE_S to Key.S,
        SDL_SCANCODE_T to Key.T,
        SDL_SCANCODE_U to Key.U,
        SDL_SCANCODE_V to Key.V,
        SDL_SCANCODE_W to Key.W,
        SDL_SCANCODE_X to Key.X,
        SDL_SCANCODE_Y to Key.Y,
        SDL_SCANCODE_Z to Key.Z,

        SDL_SCANCODE_F1 to Key.F1,
        SDL_SCANCODE_F2 to Key.F2,
        SDL_SCANCODE_F3 to Key.F3,
        SDL_SCANCODE_F4 to Key.F4,
        SDL_SCANCODE_F5 to Key.F5,
        SDL_SCANCODE_F6 to Key.F6,
        SDL_SCANCODE_F7 to Key.F7,
        SDL_SCANCODE_F8 to Key.F8,
        SDL_SCANCODE_F9 to Key.F9,
        SDL_SCANCODE_F10 to Key.F10,
        SDL_SCANCODE_F11 to Key.F11,
        SDL_SCANCODE_F12 to Key.F12,
        SDL_SCANCODE_F13 to Key.F13,
        SDL_SCANCODE_F14 to Key.F14,
        SDL_SCANCODE_F15 to Key.F15,
        SDL_SCANCODE_F16 to Key.F16,
    )
}

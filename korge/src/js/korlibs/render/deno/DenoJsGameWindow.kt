package korlibs.render.deno

import korlibs.event.Key
import korlibs.event.KeyEvent
import korlibs.graphics.AG
import korlibs.graphics.gl.AGOpengl
import korlibs.io.async.launchImmediately
import korlibs.js.Deno
import korlibs.math.geom.SizeInt
import korlibs.render.GameWindow
import korlibs.render.JsGameWindow
import korlibs.render.ffi.sdl.*

private external fun setInterval(cb: dynamic, delay: dynamic, vararg args: dynamic)

class DenoJsGameWindow : JsGameWindow() {

    override val ag: AG = AGOpengl(DenoKmlGl())

    var window: dynamic = null
    var windowWidth = 600
    var windowHeight = 600

    override fun setSize(width: Int, height: Int) {
        this.windowWidth = width
        this.windowHeight = height
        if (window != null) {
            SDL.SDL_SetWindowSize(window, width, height)
            SDL.SDL_SetWindowPosition(window, SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED)
        }
    }

    override var title: String = "Title"
        set(value) {
            field = value
            if (window != null) {
                SDL.SDL_SetWindowTitle(window, "$value\u0000".encodeToByteArray())
            }
        }
    override val width: Int get() = windowWidth
    override val height: Int get() = windowHeight

    fun getWindowSize(): SizeInt {
        val w = IntArray(1)
        val h = IntArray(1)
        SDL.SDL_GetWindowSize(window, w, h)
        return SizeInt(w[0], h[0])
    }

    private var reshapedOnce = false
    fun updateWindowSize() {
        val (w, h) = getWindowSize()
        if (windowWidth != w || windowHeight != h || !reshapedOnce) {
            reshapedOnce = true
            windowWidth = w
            windowHeight = h
            dispatchReshapeEvent(0, 0, windowWidth, windowHeight)
        }
    }

    override suspend fun loop(entry: suspend GameWindow.() -> Unit) {

        console.log("DenoJsGameWindow")

        // Open library and define exported symbols

        console.log(SDL.SDL_InitSubSystem(0x00000020))
        val flags = SDL_WINDOW_OPENGL or SDL_WINDOW_RESIZABLE
        window = SDL.SDL_CreateWindow(null, SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED, windowWidth, windowHeight, flags)
        title = this.title
        val glCtx = SDL.SDL_GL_CreateContext(window)
        //const screenSurface = SDL.SDL_GetWindowSurface(window);
        console.log(window)
        //console.log(screenSurface);
        //SDL.SDL_UpdateWindowSurface(window);

        val e = IntArray(14)
        var quit = false

        Deno.addSignalListener("SIGINT") {
            SDL.SDL_QuitSubSystem(0x00000020)
            console.log("interrupted!");
            Deno.exit();
        }

        setInterval({
            updateWindowSize()
            var n = 0
            //while (SDL.SDL_WaitEventTimeout(e, 1)) {
            //println("[a]")
            //SDL.SDL_PumpEvents(e)
            //println("[b]")
            while (SDL.SDL_PollEvent(e)) {
                if (n++ > 100) break
                val eType = e[0]
                val timestamp = e[1]
                val windowID = e[2]
                val stateRepeat = e[3]
                val scanCode = e[4]
                val keySym = e[5]
                val mod = e[6]
                //val other = e[6]
                //console.log(eType)
                when (eType) {
                    SDL_QUIT -> quit = true
                    SDL_KEYDOWN, SDL_KEYUP -> {
                        val isDown = eType == SDL_KEYDOWN
                        val key = SDL_SCANCODES.MAP[scanCode] ?: Key.UNKNOWN
                        if (key == Key.UNKNOWN) {
                            println("KEY[unknown]: eType=$eType, timestamp=$timestamp, windowID=$windowID, stateRepeat=$stateRepeat, scanCode=$scanCode, keySym=$keySym, mod=$mod")
                        }
                        val evType = if (isDown) KeyEvent.Type.DOWN else KeyEvent.Type.UP
                        dispatchKeyEvent(evType, 0, keySym.toChar(), key, scanCode)
                    }
                    SDL_MOUSEMOTION, SDL_MOUSEBUTTONDOWN, SDL_MOUSEBUTTONUP -> {
                    }
                    SDL_WINDOWEVENT -> {

                    }
                    else -> {
                        println("Unknown SDL event: $eType: ${e.toList()}")
                    }
                }
            }
            if (quit) {
                SDL.SDL_QuitSubSystem(0x00000020)
                exit()
            }
            SDL.SDL_GL_MakeCurrent(window, glCtx)

            DenoGL.glViewport(0, 0, windowWidth, windowHeight);
            DenoGL.glClearColor(1f, 1f, 0f, 1f)
            DenoGL.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

            updateWindowSize()

            frame()

            SDL.SDL_GL_SwapWindow(window)
        }, 8)


        launchImmediately(getCoroutineDispatcherWithCurrentContext()) {
            println("STARTING...")
            entry()
        }

    }

    override fun close(exitCode: Int) {
        Deno.exit()
    }
}

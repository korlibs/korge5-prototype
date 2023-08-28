package korlibs.korge

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.image.format.ImageFormat
import korlibs.image.format.RegisteredImageFormats
import korlibs.inject.Injector
import korlibs.korge.internal.DefaultViewport
import korlibs.korge.render.BatchBuilder2D
import korlibs.korge.scene.Scene
import korlibs.korge.view.Stage
import korlibs.korge.view.Views
import korlibs.math.geom.Size
import korlibs.render.GameWindow
import korlibs.time.TimeProvider
import kotlin.reflect.KClass

suspend fun Korge(
    args: Array<String> = arrayOf(),
    imageFormats: ImageFormat = RegisteredImageFormats,
    gameWindow: GameWindow? = null,
    //val eventDispatcher: EventDispatcher = gameWindow ?: DummyEventDispatcher, // Removed
    mainSceneClass: KClass<out Scene>? = null,
    timeProvider: TimeProvider = TimeProvider,
    injector: Injector = Injector(),
    configInjector: Injector.() -> Unit = {},
    debug: Boolean = false,
    trace: Boolean = false,
    context: Any? = null,
    fullscreen: Boolean? = null,
    blocking: Boolean = true,
    gameId: String = KorgeConfig.DEFAULT_GAME_ID,
    settingsFolder: String? = null,
    batchMaxQuads: Int = BatchBuilder2D.DEFAULT_BATCH_QUADS,
    windowSize: Size = DefaultViewport.SIZE,
    virtualSize: Size = windowSize,
    displayMode: KorgeDisplayMode = KorgeDisplayMode.DEFAULT,
    title: String = "Game",
    backgroundColor: RGBA? = Colors.BLACK,
    quality: GameWindow.Quality = GameWindow.Quality.PERFORMANCE,
    icon: String? = null,
    multithreaded: Boolean? = null,
    forceRenderEveryFrame: Boolean = true,
    main: (suspend Stage.() -> Unit) = {},
    debugAg: Boolean = false,
    debugFontExtraScale: Double = 1.0,
    debugFontColor: RGBA = Colors.WHITE,
    stageBuilder: (Views) -> Stage = { Stage(it) },
    targetFps: Double = 0.0,
    entry: suspend Stage.() -> Unit
): Unit = KorgeConfig(
    args = args, imageFormats = imageFormats, gameWindow = gameWindow, mainSceneClass = mainSceneClass,
    timeProvider = timeProvider, injector = injector, configInjector = configInjector, debug = debug,
    trace = trace, context = context, fullscreen = fullscreen, blocking = blocking, gameId = gameId,
    settingsFolder = settingsFolder, batchMaxQuads = batchMaxQuads,
    windowSize = windowSize, virtualSize = virtualSize,
    displayMode = displayMode, title = title, backgroundColor = backgroundColor, quality = quality,
    icon = icon,
    multithreaded = multithreaded,
    forceRenderEveryFrame = forceRenderEveryFrame,
    main = main,
    debugAg = debugAg,
    debugFontExtraScale = debugFontExtraScale,
    debugFontColor = debugFontColor,
    stageBuilder = stageBuilder,
    unit = Unit,
    targetFps = targetFps,
).start(entry)

//suspend fun Korge(entry: suspend Stage.() -> Unit) { Korge().start(entry) }

// @TODO: Doesn't compile on WASM: https://youtrack.jetbrains.com/issue/KT-58859/WASM-e-java.util.NoSuchElementException-Key-VALUEPARAMETER-namethis-typekorlibs.korge.Korge-korlibs.korge.KorgeConfig-is-missing
//suspend fun Korge(config: KorgeConfig, entry: suspend Stage.() -> Unit) { config.start(entry) }

suspend fun KorgeWithConfig(config: KorgeConfig, entry: suspend Stage.() -> Unit) { config.start(entry) }

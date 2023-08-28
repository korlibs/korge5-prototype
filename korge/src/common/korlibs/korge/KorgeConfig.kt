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
import korlibs.logger.Logger
import korlibs.math.geom.Size
import korlibs.render.GameWindow
import korlibs.time.TimeProvider
import kotlin.jvm.JvmOverloads
import kotlin.reflect.KClass

data class KorgeConfig(
    val args: Array<String> = arrayOf(),
    val imageFormats: ImageFormat = RegisteredImageFormats,
    val gameWindow: GameWindow? = null,
    //val eventDispatcher: EventDispatcher = gameWindow ?: DummyEventDispatcher, // Removed
    val mainSceneClass: KClass<out Scene>? = null,
    val timeProvider: TimeProvider = TimeProvider,
    val injector: Injector = Injector(),
    val configInjector: Injector.() -> Unit = {},
    val debug: Boolean = false,
    val trace: Boolean = false,
    val context: Any? = null,
    val fullscreen: Boolean? = null,
    val blocking: Boolean = true,
    val gameId: String = DEFAULT_GAME_ID,
    val settingsFolder: String? = null,
    val batchMaxQuads: Int = BatchBuilder2D.DEFAULT_BATCH_QUADS,
    val windowSize: Size = DefaultViewport.SIZE,
    val virtualSize: Size = windowSize,
    val displayMode: KorgeDisplayMode = KorgeDisplayMode.DEFAULT,
    val title: String = "Game",
    val backgroundColor: RGBA? = Colors.BLACK,
    val quality: GameWindow.Quality = GameWindow.Quality.PERFORMANCE,
    val icon: String? = null,
    val multithreaded: Boolean? = null,
    val forceRenderEveryFrame: Boolean = true,
    val main: (suspend Stage.() -> Unit) = {},
    val debugAg: Boolean = false,
    val debugFontExtraScale: Double = 1.0,
    val debugFontColor: RGBA = Colors.WHITE,
    val stageBuilder: (Views) -> Stage = { Stage(it) },
    val targetFps: Double = 0.0,
    val unit: Unit = Unit,
) {
    companion object {
        val logger = Logger("Korge")
        val DEFAULT_GAME_ID = "korlibs.korge.unknown"
        val DEFAULT_WINDOW_SIZE: Size get() = DefaultViewport.SIZE
    }

    suspend fun start(entry: suspend Stage.() -> Unit = this.main) {
        KorgeRunner.invoke(this.copy(main = entry))
    }
}

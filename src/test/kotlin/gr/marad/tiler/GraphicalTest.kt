package gr.marad.tiler

import gh.marad.tiler.core.*
import gh.marad.tiler.core.filteringrules.FilteringRules
import gh.marad.tiler.core.layout.OverlappingCascadeLayout
import gh.marad.tiler.core.layout.LayoutSpace
import gh.marad.tiler.core.views.ViewManager
import gh.marad.tiler.core.views.ViewSwitcher
import gr.marad.tiler.core.TestWindowId
import gr.marad.tiler.core.testIdGen
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import org.jetbrains.skija.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import kotlin.system.exitProcess

//val typeface = Typeface.makeDefault()

var mouseX = 0
var mouseY = 0
var width = 640
var height = 480
val filteringRules = FilteringRules()
//val viewManager = ViewManager { TwoColumnLayout(LayoutSpace(0, 0, width, height)) }
val viewManager = ViewManager { OverlappingCascadeLayout(20) }
val viewSwitcher = ViewSwitcher(viewManager) { desktopWindows.toDesktopState() }
val tiler = WindowsTiler(viewManager) { desktopWindows.toDesktopState() }
val eventHandler = WindowEventHandler(viewManager, tiler, filteringRules) {
    getWindowAt(mouseX, mouseY)?.let { listOf(it) } ?: emptyList()
}

val xGen2 = Arb.int(0, width -10)
val yGen2 = Arb.int(0, height -10)
val wGen2 = Arb.int(100, width /2)
val hGen2 = Arb.int(100, height /2)
val posGen2 = arbitrary {
    WindowPosition(xGen2.next(it), yGen2.next(it), wGen2.next(it), hGen2.next(it))
}
val colorPartGen = Arb.int(100, 255)
val paintGen = arbitrary {
    val paint = Paint()
    paint.setAntiAlias(true)
    paint.setARGB(255, colorPartGen.next(), colorPartGen.next(), colorPartGen.next())
}

data class WindowInfo(val window: Window, var minimized: Boolean)
var desktopWindows = mutableListOf<WindowInfo>()

fun MutableList<WindowInfo>.toDesktopState() = DesktopState(
    allWindows = this.map { it.window },
    windowsToManage = this.map { it.window },
    layoutSpace = LayoutSpace(0, 0, width, height)
)

val colors = mutableMapOf<String, Paint>()

fun init(windowHandle: Long) {

    glfwSetKeyCallback(windowHandle) { window, key, scanCode, action, mods ->
        if (key == GLFW_KEY_A && action == GLFW_PRESS) {
            val windowID = testIdGen.next()
            val wnd = Window(TestWindowId(windowID), "Name", "class", "exe_path", posGen2.next(), isMinimized = false, isMaximized = false, isPopup = false)
            colors.put(windowID, paintGen.next())
            desktopWindows.add(WindowInfo(wnd, false))
            val cmds = eventHandler.windowAppeared(wnd)
            applyCommands(cmds)
        }

        if (key == GLFW_KEY_D && action == GLFW_PRESS) {
            val windowAtCursor = getWindowAt(mouseX, mouseY) ?: desktopWindows.lastOrNull()?.window
            if (windowAtCursor != null) {
                desktopWindows.removeIf { windowAtCursor== it.window }
                val cmds = eventHandler.windowDisappeared(windowAtCursor)
                applyCommands(cmds)
            }
        }

        if (key == GLFW_KEY_M && action == GLFW_PRESS) {
            val windowAtCursor = getWindowAt(mouseX, mouseY)
            if (windowAtCursor != null) {
                desktopWindows.replaceAll {
                    if (it.window.id == windowAtCursor.id) {
                        it.copy(minimized = true)
                    } else { it }
                }
                eventHandler.windowMinimized(windowAtCursor)
                    .also { applyCommands(it) }
            }
        }

        if (key == GLFW_KEY_R && action == GLFW_PRESS) {
            val windowToRestore = desktopWindows.find { it.minimized }?.window
            if (windowToRestore != null) {
                desktopWindows.replaceAll {
                    if (it.window.id == windowToRestore.id) {
                        it.copy(minimized = false)
                    } else { it }
                }
                eventHandler.windowRestored(windowToRestore)
                    .also(::applyCommands)
            }
        }


        if (key in GLFW_KEY_0..GLFW_KEY_9 && action == GLFW_PRESS) {
            val viewId = key - GLFW_KEY_0 - 1
            val cmds = viewSwitcher.switchToView(viewId)
            applyCommands(cmds)
        }

        if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
            glfwTerminate()
            exitProcess(0)
        }
    }


    glfwSetCursorPosCallback(windowHandle) { window, x, y ->
        mouseX = x.toInt()
        mouseY = y.toInt()
    }

    glfwSetMouseButtonCallback(windowHandle) { window, button, action, mods ->
        println("Mouse: $button, $action, $mods")
        println("Pos: $mouseX, $mouseY")
    }

}

fun getWindowAt(x: Int, y: Int): Window? {
    return desktopWindows.filter {
        val pos = it.window.position
        !it.minimized &&
                x >= pos.x && x <= pos.x + pos.width &&
                y >= pos.y && y <= pos.y + pos.height
    }.singleOrNull()?.window
}


fun applyCommands(cmds: List<TilerCommand>) {
    cmds.forEach { cmd ->
        when(cmd) {
            is SetWindowPosition -> {
                desktopWindows.replaceAll {
                    if (it.window.id == cmd.windowId) {
                        it.copy(minimized = false, window = it.window.copy(position = cmd.position))
                    } else {
                        it
                    }
                }
            }
            is MinimizeWindow -> {
                desktopWindows.replaceAll {
                    if (it.window.id == cmd.windowId) {
                        it.copy(minimized = true)
                    } else { it }
                }
            }

            is ActivateWindow -> TODO()
            is ShowWindow -> TODO()
        }
    }
}


fun render(canvas: Canvas) {
    val paint = Paint()
    paint.color = Color.makeARGB(255, 50, 100, 50)
    paint.isAntiAlias = true
    canvas.clear(0x000000)
    canvas.drawCircle(width /2f, height /2f, height /2f, paint)


    desktopWindows.forEach { windowInfo ->
        if (!windowInfo.minimized) {
            val window = windowInfo.window
            val rect = Rect(
                window.position.x.toFloat(),
                window.position.y.toFloat(),
                (window.position.x + window.position.width).toFloat(),
                (window.position.y + window.position.height).toFloat(),
            )
            canvas.drawRect(rect, colors[(window.id as TestWindowId).id]!!)
        }
    }

//    val font = Font(typeface, 13f)
//    canvas.drawString("Hello World!", 10f, 10f, font, paint)
}

fun main() {
//    val surface = Surface.makeRasterN32Premul(100, 100)
//    val canvas = surface.canvas
//    val paint = Paint()
//    paint.color = 0xFFFF00
//    canvas.drawCircle(50f, 50f, 30f, paint)
//
//    val image = surface.makeImageSnapshot()
//    val pngData = image.encodeToData(EncodedImageFormat.PNG)
//    val pngBytes = pngData.toByteBuffer()


    glfwInit()
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
    val windowHandle = glfwCreateWindow(width, height, "Test", 0, 0)
    glfwMakeContextCurrent(windowHandle)
    glfwSwapInterval(1)
    glfwShowWindow(windowHandle)

    GL.createCapabilities()
    val context = DirectContext.makeGL()
    val fbId = GL11.glGetInteger(0x8CA6) // GL_FRAMEBUFFER_BINDING
    var renderTarget = BackendRenderTarget.makeGL(
        width,
        height,
        0, // samples
        8, // stencil
        fbId,
        FramebufferFormat.GR_GL_RGBA8
    )


    var surface = Surface.makeFromBackendRenderTarget(
        context,
        renderTarget,
        SurfaceOrigin.BOTTOM_LEFT,
        SurfaceColorFormat.RGBA_8888,
        ColorSpace.getSRGB()
    )

    // reset viewport, render target and surface when window size changes
    glfwSetWindowSizeCallback(windowHandle) { window, w, h ->
        width = w
        height = h
        GL11.glViewport(0, 0, w, h)

        renderTarget = BackendRenderTarget.makeGL(
            width,
            height,
            0, // samples
            8, // stencil
            fbId,
            FramebufferFormat.GR_GL_RGBA8
        )


        surface = Surface.makeFromBackendRenderTarget(
            context,
            renderTarget,
            SurfaceOrigin.BOTTOM_LEFT,
            SurfaceColorFormat.RGBA_8888,
            ColorSpace.getSRGB()
        )

        println("Retiling!")
        applyCommands(tiler.retile())
    }

    init(windowHandle)

    while(!glfwWindowShouldClose(windowHandle)) {
        render(surface.canvas)
        context.flush()
        glfwSwapBuffers(windowHandle)
        glfwPollEvents()
    }
}

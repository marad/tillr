package gh.marad.tiler.core

sealed interface TilerCommand

/**
 * Sets window position. It should also un-minimize the window if necessary
 */
data class SetWindowPosition(val windowId: WindowId, val position: WindowPosition) : TilerCommand

/**
 * Minimizes given window
 */
data class MinimizeWindow(val windowId: WindowId) : TilerCommand
data class ShowWindow(val windowId: WindowId) : TilerCommand
data class ActivateWindow(val windowId: WindowId) : TilerCommand

/**
 * Represents a window identifier
 */
interface WindowId
typealias Windows = List<Window>

data class WindowPosition(val x: Int, val y: Int, val width: Int, val height: Int)

data class Window(val id: WindowId,
                  val windowName: String,
                  val className: String,
                  val exePath: String,
                  val position: WindowPosition,
                  val isMinimized: Boolean,
                  val isMaximized: Boolean,
                  val isPopup: Boolean,
    ) {
    val exeName = exePath.split("\\").last()
    fun reposition(x: Int?, y: Int?, width: Int? = null, height: Int? = null): Window {
        return copy(position = WindowPosition(
            x = x ?: position.x,
            y = y ?: position.y,
            width = width ?: position.width,
            height = height ?: position.height
        )
        )
    }
}

data class DesktopState(
    val windows: Windows,
)
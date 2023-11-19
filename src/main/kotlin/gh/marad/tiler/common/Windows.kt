package gh.marad.tiler.common

/**
 * Represents a window identifier
 */
interface WindowId
typealias Windows = List<Window>

data class WindowPosition(val x: Int, val y: Int, val width: Int, val height: Int) {
    fun centerX() = x + (width / 2)
    fun centerY() = y + (height / 2)
    val left get() = x
    val right get() = x + width
    val top get() = y
    val bottom get() = y + height
}
data class Window(val id: WindowId,
                  val windowName: String,
                  val className: String,
                  val exePath: String,
                  val position: WindowPosition,
                  val isMinimized: Boolean,
                  val isMaximized: Boolean,
                  val isPopup: Boolean,
                  val isActive: Boolean,
                  val isVisible: Boolean
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
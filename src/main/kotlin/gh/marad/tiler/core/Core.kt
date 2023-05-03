package gh.marad.tiler.core

interface WindowManager {
    fun listWindows(): Windows
}

sealed interface TilerCommand

/**
 * Sets window position. It should also un-minimize the window if necessary
 */
data class SetWindowPosition(val windowId: WindowId, val position: WindowPosition) : TilerCommand

/**
 * Minimizes given window
 */
data class MinimizeWindow(val windowId: WindowId) : TilerCommand

/**
 * Represents a window identifier
 */
interface WindowId
interface Layout {
    fun updateSpace(space: LayoutSpace)
    fun retile(windows: Windows): Windows
}

data class LayoutSpace(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

class VerticalStackLayout(private var space: LayoutSpace) : Layout {
    override fun retile(windows: Windows): Windows {
        if (windows.isEmpty()) return emptyList()

        var posY = 0
        val height = space.height / windows.size
        return windows.map {
            it.reposition(
                x = space.x,
                y = space.y + posY,
                width = space.width,
                height = height
            ).also {
                posY += height
            }
        }
    }

    override fun updateSpace(space: LayoutSpace) {
        this.space = space
    }
}

class TwoColumnLayout(space: LayoutSpace) : Layout {

    private var rightColumnLayout = VerticalStackLayout(calcRightColumnSpace(space))
    private var columnWidth = space.width / 2
    private var space = space
        set(value) {
            field = value
            rightColumnLayout = VerticalStackLayout(calcRightColumnSpace(space))
            columnWidth = space.width / 2
        }

    override fun updateSpace(space: LayoutSpace) {
        this.space = space
    }

    private fun calcRightColumnSpace(space: LayoutSpace) = LayoutSpace(
        x = space.x + space.width/2,
        y = space.y,
        width = space.width/2,
        height = space.height
    )

    override fun retile(windows: Windows): Windows {
        if (windows.isEmpty()) return emptyList()
        if (windows.size == 1) {
            return listOf(windows.first().reposition(space.x, space.y, space.width, space.height))
        }

        val firstWindow = windows.first().reposition(0, 0, columnWidth, space.height)
        val others = rightColumnLayout.retile(windows.drop(1))
        return listOf(firstWindow) + others
    }

}



typealias Windows = List<Window>

data class WindowPosition(val x: Int, val y: Int, val width: Int, val height: Int)
data class Window(val id: WindowId,
                  val windowName: String,
                  val className: String,
                  val position: WindowPosition) {
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



fun activate(allManagedWindows: Windows, windowsInView: Windows): List<TilerCommand> {
    val windowsById = windowsInView.associateBy { it.id }
    val viewWindowIds = windowsById.keys
    val commands = mutableListOf<TilerCommand>()
    allManagedWindows.forEach {
        if (it.id in viewWindowIds) {
            commands += SetWindowPosition(it.id, windowsById[it.id]!!.position)
        } else {
            commands += MinimizeWindow(it.id)
        }
    }
    return commands
}

fun calcWindowMovements(windowsInView: Windows, positionedWindows: Windows): List<TilerCommand> {
    val positionedWindowsById = positionedWindows.associateBy { it.id }
    val commands = mutableListOf<TilerCommand>()
    windowsInView.forEach {
        val positionedWindow = positionedWindowsById[it.id]
        if (positionedWindow != null && it.position != positionedWindow.position) {
            commands.add(SetWindowPosition(it.id, positionedWindow.position))
        }
    }
    return commands
}

class View(
    private var windows: MutableList<Window> = mutableListOf(),
    layout: Layout
) : Iterable<Window> {

    var layout = layout

    fun addWindow(window: Window) {
        if (!windows.any { it.id == window.id }) {
            windows.add(window)
            retile()
        }
    }

    fun removeWindow(windowId: WindowId) {
        windows.removeIf { it.id == windowId }
        retile()
    }

    fun retile() {
        windows = layout.retile(windows).toMutableList()
    }

    override fun iterator(): Iterator<Window> = windows.iterator()
}

class ViewManager(private val defaultLayout: Layout) {
    private var _activeViewId: Int = 0
    private val _views = mutableMapOf(0 to View(layout = defaultLayout))

    fun updateSpace(space: LayoutSpace) {
        defaultLayout.updateSpace(space)
        _views.values.forEach {
            it.layout.updateSpace(space)
        }
    }

    private fun currentView(): View  {
      return _views.getOrPut(_activeViewId) { View(layout = defaultLayout) }
    }

    fun addWindow(window: Window) {
        currentView().addWindow(window)
    }

    fun removeWindow(windowId: WindowId) {
        currentView().removeWindow(windowId)
    }

    fun currentViewWindows(): Windows = currentView().toList()

    fun retile(): Windows {
        currentView().retile()
        return currentViewWindows()
    }

    fun changeCurrentView(viewId: Int) { _activeViewId = viewId }
}

data class DesktopState(
    val windows: Windows,
)

class Tiler(defaultLayout: Layout) {
    private val views = ViewManager(defaultLayout)

    fun updateSpace(space: LayoutSpace) {
        views.updateSpace(space)
    }

    fun activateView(viewId: Int, desktopState: DesktopState): List<TilerCommand> {
        views.changeCurrentView(viewId)
        return activate(desktopState.windows, views.currentViewWindows())
    }

    fun retile(desktopState: DesktopState): List<TilerCommand> {
        val positionedWindows = views.retile()
        return calcWindowMovements(desktopState.windows, positionedWindows)
    }

    fun windowAppeared(window: Window, desktopState: DesktopState): List<TilerCommand> {
        views.addWindow(window)
        return retile(desktopState)
    }

    fun windowDisappeared(window: Window, desktopState: DesktopState): List<TilerCommand> {
        views.removeWindow(window.id)
        return retile(desktopState)
    }

    fun windowMinimized(window: Window, desktopState: DesktopState): List<TilerCommand> {
        views.removeWindow(window.id)
        return retile(desktopState)
    }

    fun windowMaximized(window: Window, desktopState: DesktopState): List<TilerCommand> {
        return emptyList()
    }

    fun windowRestored(window: Window, desktopState: DesktopState): List<TilerCommand> {
        views.addWindow(window)
        return retile(desktopState)
    }
}
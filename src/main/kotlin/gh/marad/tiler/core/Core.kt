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

data class ShowWindow(val windowId: WindowId) : TilerCommand

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
                  val exePath: String,
                  val position: WindowPosition,
                  val minimized: Boolean,
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

fun calcWindowMovements(allWindows: Windows, positionedWindows: Windows): List<TilerCommand> {
    val positionedWindowsById = positionedWindows.associateBy { it.id }
    val commands = mutableListOf<TilerCommand>()
    allWindows.forEach {
        val positionedWindow = positionedWindowsById[it.id]
        if (positionedWindow != null && it.position != positionedWindow.position) {
            commands.add(SetWindowPosition(it.id, positionedWindow.position))
        }
    }
    return commands
}

class View(
    private var windows: MutableList<WindowId> = mutableListOf(),
    var layout: Layout
) {

    fun addWindow(windowId: WindowId) {
        if (!windows.contains(windowId)) {
            windows.add(windowId)
        }
    }

    fun removeWindow(windowId: WindowId) {
        windows.removeIf { it == windowId }
    }

    fun hasWindow(windowId: WindowId): Boolean = windows.contains(windowId)

    fun filterWindowsInView(windows: Windows): Windows {
        // it's done this way instead of `windows.filter` to preserve
        // the ordering of windows in the view
        val windowsById = windows.associateBy { it.id }
        return this.windows.mapNotNull { windowsById[it] }
    }

    fun filterWindowsNotInView(windows: Windows): Windows = windows.filterNot { hasWindow(it.id) }

    fun updateWindowsInView(windows: List<WindowId>) {
        this.windows.clear()
        this.windows.addAll(windows)
    }

    fun swapWindows(a: WindowId, b: WindowId) {
        val aIdx = windows.indexOf(a)
        val bIdx = windows.indexOf(b)
        if (aIdx < 0 || bIdx < 0) return
        val tmp = windows[aIdx]
        windows[aIdx] = windows[bIdx]
        windows[bIdx] = tmp
    }

    fun debugGetWindowsInView(): List<WindowId> = windows
}

class ViewManager(private val defaultLayout: Layout) {
    private var _activeViewId: Int = 0
    private val _views = mutableMapOf(0 to View(layout = defaultLayout))

    fun updateLayoutSpace(space: LayoutSpace) {
        defaultLayout.updateSpace(space)
        _views.values.forEach {
            it.layout.updateSpace(space)
        }
    }

    fun currentView(): View  {
      return _views.getOrPut(_activeViewId) { View(layout = defaultLayout) }
    }

    fun changeCurrentView(viewId: Int): View {
        _activeViewId = viewId
        return currentView()
    }
}

data class DesktopState(
    val windows: Windows,
)

class Tiler(defaultLayout: Layout, private val getDesktopState: () -> DesktopState) {
    private val views = ViewManager(defaultLayout)

    fun updateSpace(space: LayoutSpace) {
        views.updateLayoutSpace(space)
    }

    fun activateView(viewId: Int): List<TilerCommand> {
        val desktopState = getDesktopState()
        val view = views.changeCurrentView(viewId)
        val showCommands = view.filterWindowsInView(desktopState.windows)
            .filter { it.minimized }
            .map { ShowWindow(it.id) }
        val minimizeCommands = view.filterWindowsNotInView(desktopState.windows)
            .filterNot { it.minimized }
            .map { MinimizeWindow(it.id) }
        return minimizeCommands + showCommands + retile()
    }

    fun retile(): List<TilerCommand> {
        val desktopState = getDesktopState()
        val view = views.currentView()
        desktopState.windows.filterNot { it.minimized }
            .forEach { view.addWindow(it.id) }
        val windowsInView = view.filterWindowsInView(desktopState.windows)
        view.updateWindowsInView(windowsInView.map { it.id })
        val positionedWindows = view.layout.retile(windowsInView)
        return calcWindowMovements(desktopState.windows, positionedWindows)
    }

    fun windowAppeared(window: Window): List<TilerCommand> {
        views.currentView().addWindow(window.id)
        return retile()
    }

    fun windowDisappeared(window: Window): List<TilerCommand> {
        views.currentView().removeWindow(window.id)
        return retile()
    }

    fun windowMinimized(window: Window): List<TilerCommand> {
        views.currentView().removeWindow(window.id)
        return retile()
    }

    fun windowRestored(window: Window): List<TilerCommand> {
        views.currentView().addWindow(window.id)
        return retile()
    }

    fun swapWindows(a: WindowId, b: WindowId) {
        views.currentView().swapWindows(a,b)
    }

    fun inView(id: WindowId): Boolean =
        views.currentView().hasWindow(id)

    fun debugGetWindowsInView(): List<WindowId> {
        return views.currentView().debugGetWindowsInView()
    }
}
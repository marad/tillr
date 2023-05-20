package gh.marad.tiler.core

import gh.marad.tiler.core.views.ViewManager

class WindowEventHandler(
    private val viewManager: ViewManager,
    private val windowsTiler: WindowsTiler,
    private val windowsUnderCursor: () -> List<Window>,
) {
    fun windowActivated(window: Window): List<TilerCommand> {
        viewManager.currentView().addWindow(window.id)
        return windowsTiler.retile()
    }

    fun windowAppeared(window: Window): List<TilerCommand> {
        viewManager.currentView().addWindow(window.id)
        return windowsTiler.retile()
    }

    fun windowDisappeared(window: Window): List<TilerCommand> {
        viewManager.currentView().removeWindow(window.id)
        return windowsTiler.retile()
    }

    fun windowMinimized(window: Window): List<TilerCommand> {
        viewManager.currentView().removeWindow(window.id)
        return windowsTiler.retile()
    }

    fun windowRestored(window: Window): List<TilerCommand> {
        viewManager.currentView().addWindow(window.id)
        return windowsTiler.retile()
    }

    fun windowMovedOrResized(window: Window): List<TilerCommand> {
        val foundWindow = windowsUnderCursor().lastOrNull()
        if (foundWindow != null && foundWindow.id != window.id) {
            viewManager.currentView().swapWindows(window.id, foundWindow.id)
        }
        return windowsTiler.retile()
    }

}
package gh.marad.tiler.core

import gh.marad.tiler.core.filteringrules.FilteringRules
import gh.marad.tiler.core.views.ViewManager
import gh.marad.tiler.os.OsFacade

class WindowEventHandler(
    private val viewManager: ViewManager,
    private val windowsTiler: WindowsTiler,
    private val filteringRules: FilteringRules,
    private val os: OsFacade,
) {
    fun windowActivated(window: Window) {
        if (filteringRules.shouldManage(window)) {
            viewManager.currentView().addWindow(window.id)
        }
        os.execute(windowsTiler.retile())
    }

    fun windowAppeared(window: Window) {
        if (filteringRules.shouldManage(window)) {
            viewManager.currentView().addWindow(window.id)
        }
        os.execute(windowsTiler.retile())
    }

    fun windowDisappeared(window: Window) {
        viewManager.currentView().removeWindow(window.id)
        os.execute(windowsTiler.retile())
    }

    fun windowMinimized(window: Window) {
        viewManager.currentView().removeWindow(window.id)
        os.execute(windowsTiler.retile())
    }

    fun windowRestored(window: Window) {
        if (filteringRules.shouldManage(window)) {
            viewManager.currentView().addWindow(window.id)
        }
        os.execute(windowsTiler.retile())
    }

    fun windowMovedOrResized(window: Window) {
        val foundWindow = os.windowsUnderCursor().lastOrNull()
        if (foundWindow != null && foundWindow.id != window.id) {
            viewManager.currentView().swapWindows(window.id, foundWindow.id)
        }
        os.execute(windowsTiler.retile())
    }

}
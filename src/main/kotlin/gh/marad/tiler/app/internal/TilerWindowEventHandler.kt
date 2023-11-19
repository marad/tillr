package gh.marad.tiler.app.internal

import gh.marad.tiler.common.filteringrules.FilteringRules
import gh.marad.tiler.os.OsFacade
import gh.marad.tiler.common.Window
import gh.marad.tiler.os.WindowEventHandler
import gh.marad.tiler.tiler.TilerFacade

class TilerWindowEventHandler(
    private val tiler: TilerFacade,
    private val filteringRules: FilteringRules,
    private val os: OsFacade,
    private val executor: TilerCommandsExecutorAndWatcher
): WindowEventHandler {
    override fun windowActivated(window: Window) {
        if (filteringRules.shouldManage(window)) {
            executor.execute(tiler.addWindow(window))
        }
    }

    override fun windowAppeared(window: Window) {
        if (filteringRules.shouldManage(window)) {
            executor.execute(tiler.addWindow(window))
        }
    }

    override fun windowDisappeared(window: Window) {
        executor.execute(tiler.removeWindow(window))
    }

    override fun windowMinimized(window: Window) {
        executor.execute(tiler.removeWindow(window))
    }

    override fun windowRestored(window: Window) {
        if (filteringRules.shouldManage(window)) {
            executor.execute(tiler.addWindow(window))
        }
    }

    override fun windowMovedOrResized(window: Window) {
        if (!filteringRules.shouldManage(window)) return
        val foundWindow = os.windowsUnderCursor().lastOrNull { it.isVisible }
        if (foundWindow != null && foundWindow.id != window.id) {
            executor.execute(tiler.swapWindows(window, foundWindow))
        } else {
            executor.execute(tiler.retile())
        }
    }

}
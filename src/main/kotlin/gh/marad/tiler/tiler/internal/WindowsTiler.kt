package gh.marad.tiler.tiler.internal

import gh.marad.tiler.tiler.internal.views.ViewManager
import gh.marad.tiler.tiler.internal.views.ViewSwitcher
import gh.marad.tiler.common.DesktopState
import gh.marad.tiler.common.MinimizeWindow
import gh.marad.tiler.common.TilerCommand
import gh.marad.tiler.common.Window
import gh.marad.tiler.tiler.TilerFacade
import gh.marad.tiler.common.Window as TilerWindow

class WindowsTiler(
    private val viewManager: ViewManager,
    private val getDesktopState: () -> DesktopState
): TilerFacade {
    private val viewSwitcher = ViewSwitcher(viewManager, getDesktopState)
    override var enabled = true

    override fun initializeWithOpenWindows(): List<TilerCommand> {
        if (!enabled) return emptyList()
        viewManager.changeCurrentView(0)
        getDesktopState().windowsToManage.forEach {
            if (!it.isPopup && !it.isMinimized) {
                viewManager.currentView().addWindow(it.id)
            }
        }
        return retile()
    }

    override fun switchToView(viewId: Int): List<TilerCommand> {
        if (!enabled) return emptyList()
        return viewSwitcher.switchToView(viewId)
    }

    override fun switchToPreviousView(): List<TilerCommand> {
        if (!enabled) return emptyList()
        return viewSwitcher.switchToPreviousView()
    }

    override fun addWindow(window: Window): List<TilerCommand> {
        viewManager.currentView().addWindow(window.id)
        return retile()
    }

    override fun removeWindow(window: Window): List<TilerCommand> {
        viewManager.currentView().removeWindow(window.id)
        return retile()
    }

    override fun moveWindow(window: TilerWindow, viewId: Int): List<TilerCommand> {
        if (!enabled) return emptyList()
        viewManager.moveWindow(window.id, viewId)
        return (listOf(MinimizeWindow(window.id)) + retile())
    }

    override fun swapWindows(first: Window, second: Window): List<TilerCommand> {
        viewManager.currentView().swapWindows(first.id, second.id)
        return retile()
    }

    override fun retile(): List<TilerCommand> {
        if (!enabled) return emptyList()
        val view = viewManager.currentView()
        return retile(view, getDesktopState().windowsToManage, getDesktopState().layoutSpace)
    }

}
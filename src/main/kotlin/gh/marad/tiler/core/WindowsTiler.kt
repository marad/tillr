package gh.marad.tiler.core

import gh.marad.tiler.core.views.ViewManager
import gh.marad.tiler.core.views.ViewSwitcher
import gh.marad.tiler.core.Window as TilerWindow

class WindowsTiler(
    private val viewManager: ViewManager,
    private val getDesktopState: () -> DesktopState
) {
    private val viewSwitcher = ViewSwitcher(viewManager, getDesktopState)

    fun initializeWithOpenWindows(): List<TilerCommand> {
        viewManager.changeCurrentView(0)
        getDesktopState().windowsToManage.forEach {
            if (!it.isPopup && !it.isMinimized) {
                viewManager.currentView().addWindow(it.id)
            }
        }
        return retile()
    }

    fun switchToView(viewId: Int): List<TilerCommand> {
        return viewSwitcher.switchToView(viewId)
    }

    fun switchToPreviousView(): List<TilerCommand> {
        return viewSwitcher.switchToPreviousView()
    }

    fun moveWindow(window: TilerWindow, viewId: Int): List<TilerCommand> {
        viewManager.moveWindow(window.id, viewId)
        return (listOf(MinimizeWindow(window.id)) + retile())
    }

    fun retile(): List<TilerCommand> {
        val view = viewManager.currentView()
        return retile(view, getDesktopState().windowsToManage, getDesktopState().layoutSpace)
    }

}
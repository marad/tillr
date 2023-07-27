package gh.marad.tiler.tiler.internal.views

import gh.marad.tiler.common.*

class ViewSwitcher(private val viewManager: ViewManager, val getDesktopState: () -> DesktopState) {
    private var activeView: Int? = null
    private var previousView: Int? = null

    fun switchToView(viewId: Int): List<TilerCommand> {
        previousView = activeView
        activeView = viewId
        return activateView(viewId)
    }

    fun switchToPreviousView(): List<TilerCommand> {
        val previousView = previousView
        return if (previousView != null) switchToView(previousView)
        else emptyList()
    }

    private fun activateView(viewId: Int): List<TilerCommand> {
        val desktopState = getDesktopState()
        val view = viewManager.changeCurrentView(viewId)
        val showCommands = view.filterWindowsInView(desktopState.allWindows)
            .filter { it.isMinimized }
            .map { ShowWindow(it.id) }
        val minimizeCommands = view.filterWindowsNotInView(desktopState.allWindows)
            .filterNot { it.isMinimized }
            .map { MinimizeWindow(it.id) }
        val windowToActivate = view.windowToActivate()

        return (minimizeCommands + showCommands)
            .let {
                if (windowToActivate != null) {
                    it + ActivateWindow(windowToActivate)
                } else it
            }
    }
}
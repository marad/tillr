package gh.marad.tiler.tiler.internal.views

import gh.marad.tiler.common.*
import gh.marad.tiler.common.filteringrules.FilteringRules

class ViewSwitcher(private val viewManager: ViewManager, private val filteringRules: FilteringRules, val getDesktopState: () -> DesktopState) {
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
        val windowsInView = view.filterWindowsInView(desktopState.windows)
        view.updateWindowsInView(windowsInView.map { it.id }) // this clears any inactive windowIds stored in view
        val showCommands = windowsInView
            .filter { it.isMinimized || !it.isVisible }
            .map { ShowWindow(it.id) }
        val minimizeCommands = view.filterWindowsNotInView(desktopState.getManagableWindows(filteringRules))
            .filterNot { it.isMinimized || !it.isVisible }
            .map { HideWindow(it.id) }
        val windowToActivate = view.windowToActivate()

        return (minimizeCommands + showCommands)
            .let {
                if (windowToActivate != null) {
                    it + ActivateWindow(windowToActivate)
                } else it
            }
    }
}
package gh.marad.tiler.core.views

import gh.marad.tiler.core.DesktopState
import gh.marad.tiler.core.TilerCommand
import gh.marad.tiler.core.activateView

class ViewSwitcher(private val viewManager: ViewManager, val getDesktopState: () -> DesktopState) {
    private var activeView: Int? = null
    private var previousView: Int? = null

    fun switchToView(viewId: Int): List<TilerCommand> {
        previousView = activeView
        activeView = viewId
        return activateView(viewId, viewManager, getDesktopState())
    }

    fun switchToPreviousView(): List<TilerCommand> {
        val previousView = previousView
        return if (previousView != null) switchToView(previousView)
        else emptyList()
    }
}
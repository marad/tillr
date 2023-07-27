package gh.marad.tiler.tiler.internal.views

import gh.marad.tiler.common.DesktopState
import gh.marad.tiler.common.TilerCommand
import gh.marad.tiler.tiler.internal.activateView

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
package gh.marad.tiler.tiler.internal

import gh.marad.tiler.common.*
import gh.marad.tiler.common.Window
import gh.marad.tiler.common.filteringrules.FilteringRules
import gh.marad.tiler.common.layout.Layout
import gh.marad.tiler.tiler.internal.views.ViewManager
import gh.marad.tiler.tiler.internal.views.ViewSwitcher
import gh.marad.tiler.common.layout.LayoutSpace
import gh.marad.tiler.os.OsFacade
import gh.marad.tiler.tiler.TilerFacade
import gh.marad.tiler.tiler.internal.views.View
import gh.marad.tiler.common.Window as TilerWindow

class Tiler(
    private val viewManager: ViewManager,
    private val filteringRules: FilteringRules,
    private val os: OsFacade,
): TilerFacade {
    private val viewSwitcher = ViewSwitcher(viewManager, filteringRules, os::getDesktopState)
    override var enabled = true

    override fun initializeWithOpenWindows(): List<TilerCommand> {
        if (!enabled) return emptyList()
        viewManager.changeCurrentView(0)
        os.getDesktopState().getManagableWindows(filteringRules).forEach {
            if (!it.isPopup && !it.isMinimized) {
                viewManager.currentView().addWindow(it.id)
            }
        }
        return retile()
    }

    override fun switchToView(viewId: Int): List<TilerCommand> {
        if (!enabled) return emptyList()
        return viewSwitcher.switchToView(viewId) + retile()
    }

    override fun switchToPreviousView(): List<TilerCommand> {
        if (!enabled) return emptyList()
        return viewSwitcher.switchToPreviousView() + retile()
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
        val desktopState = os.getDesktopState()
        val windows = desktopState.getManagableWindows(filteringRules)
            .filterNot { it.isMinimized }
        val assignedWindows = assignWindowsToLayoutSpaces(windows, desktopState.monitors)
        return assignedWindows.flatMap { retile(view, it.value, it.key) }
    }

    override fun currentViewLayout(): Layout {
        return viewManager.currentView().layout
    }

    private fun assignWindowsToLayoutSpaces(windows: Windows, monitors: List<Monitor>): Map<LayoutSpace, Windows> {
        val defaultSpace = monitors.find { it.isPrimary }?.layoutSpace ?: monitors.first().layoutSpace
        val spaces = monitors.map { it.layoutSpace }
        return windows
            .map { window: Window ->
                val space = spaces.find { it.containsWindowCenter(window) } ?: defaultSpace
                space to window
            }
            .groupBy({ it.first }, { it.second })
    }

    private fun LayoutSpace.containsWindowCenter(window: Window) = contains(window.position.centerX(), window.position.centerY())
    private fun WindowPosition.centerX() = (x + width) / 2
    private fun WindowPosition.centerY() = (y + height) / 2

    private fun retile(view: View, windows: Windows, space: LayoutSpace): List<TilerCommand> {
        val windowsInView = view.filterWindowsInView(windows)
//        view.updateWindowsInView(windowsInView.map { it.id })
        val movedWindows = view.layout.retile(windowsInView, space)
        return setPositionOnlyWhenWindowMoved(windows, movedWindows)
    }

    private fun setPositionOnlyWhenWindowMoved(windows: Windows, movedWindows: Windows): List<TilerCommand> {
        val positionedWindowsById = movedWindows.associateBy { it.id }
        val commands = mutableListOf<TilerCommand>()
        windows.forEach {
            val positionedWindow = positionedWindowsById[it.id]
            if (positionedWindow != null && it.position != positionedWindow.position) {
                commands.add(SetWindowPosition(it.id, positionedWindow.position))
            }
        }
        return commands
    }
}
package gh.marad.tiler.tiler.internal

import gh.marad.tiler.common.*
import gh.marad.tiler.common.Window
import gh.marad.tiler.common.assignments.WindowAssignments
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
    private val assignments: WindowAssignments,
    private val os: OsFacade,
): TilerFacade {
    private val viewSwitcher = ViewSwitcher(viewManager, filteringRules, os::getDesktopState)
    override var enabled = true

    override fun initializeWithOpenWindows(): List<TilerCommand> {
        if (!enabled) return emptyList()
        viewManager.changeCurrentView(0)
        os.getDesktopState().getManagableWindows(filteringRules).reversed().forEach {
            if (!it.isPopup && !it.isMinimized && it.isVisible) {
                viewManager.currentView().addWindow(it.id)
            }

            assignments.getAssignmentForWindow(it)?.let { assignment ->
                viewManager.getView(assignment.viewId).addWindow(it.id)
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
        val wasNotAssignedBefore = !viewManager.isWindowAssigned(window.id)
        val assignment = assignments.getAssignmentForWindow(window)
        if (wasNotAssignedBefore && assignment != null) {
            viewManager.addWindowToView(window.id, assignment.viewId)
        }
        viewManager.currentView().addWindow(window.id)
        return retile()
    }

    override fun removeWindow(window: Window): List<TilerCommand> {
        viewManager.currentView().removeWindow(window.id)
        return retile()
    }

    override fun moveWindow(window: TilerWindow, viewId: Int): List<TilerCommand> {
        if (!enabled || viewId == viewManager.currentViewId) return emptyList()
        viewManager.moveWindow(window.id, viewId)
        val windowToActivate = viewManager.currentView().windowToActivate()
        return if (!viewManager.currentView().hasWindow(os.activeWindow().id) && windowToActivate != null) {
            (listOf(HideWindow(window.id), ActivateWindow(windowToActivate)) + retile())
        } else {
            (listOf(HideWindow(window.id)) + retile())
        }
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

    private fun retile(view: View, windows: Windows, space: LayoutSpace): List<TilerCommand> {
        val windowsInView = view.filterWindowsInView(windows)
        view.updateWindowsInView(windowsInView.map { it.id })
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
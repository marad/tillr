package gh.marad.tiler.tiler.internal

import gh.marad.tiler.common.*
import gh.marad.tiler.common.Window
import gh.marad.tiler.common.filteringrules.FilteringRules
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
        return retile(view, desktopState.getManagableWindows(filteringRules), desktopState.layoutSpace)
    }

    private fun retile(view: View, allWindows: Windows, space: LayoutSpace): List<TilerCommand> {
        allWindows.filterNot { it.isMinimized }
            .forEach { view.addWindow(it.id) }
        val windowsInView = view.filterWindowsInView(allWindows)
        view.updateWindowsInView(windowsInView.map { it.id })
        val positionedWindows = view.layout.retile(windowsInView, space)
        return createPositionCommands(allWindows, positionedWindows)
    }

    private fun createPositionCommands(allWindows: Windows, positionedWindows: Windows): List<TilerCommand> {
        val positionedWindowsById = positionedWindows.associateBy { it.id }
        val commands = mutableListOf<TilerCommand>()
        allWindows.forEach {
            val positionedWindow = positionedWindowsById[it.id]
            if (positionedWindow != null && it.position != positionedWindow.position) {
                commands.add(SetWindowPosition(it.id, positionedWindow.position))
            }
        }
        return commands
    }
}
package gh.marad.tiler.tiler.internal

import gh.marad.tiler.common.layout.LayoutSpace
import gh.marad.tiler.tiler.internal.views.View
import gh.marad.tiler.tiler.internal.views.ViewManager
import gh.marad.tiler.common.*


fun createPositionCommands(allWindows: Windows, positionedWindows: Windows): List<TilerCommand> {
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

fun retile(view: View, allWindows: Windows, space: LayoutSpace): List<TilerCommand> {
    allWindows.filterNot { it.isMinimized }
        .forEach { view.addWindow(it.id) }
    val windowsInView = view.filterWindowsInView(allWindows)
    view.updateWindowsInView(windowsInView.map { it.id })
    val positionedWindows = view.layout.retile(windowsInView, space)
    return createPositionCommands(allWindows, positionedWindows)
}

fun activateView(viewId: Int, viewManager: ViewManager, desktopState: DesktopState): List<TilerCommand> {
    val view = viewManager.changeCurrentView(viewId)
    val showCommands = view.filterWindowsInView(desktopState.allWindows)
        .filter { it.isMinimized }
        .map { ShowWindow(it.id) }
    val minimizeCommands = view.filterWindowsNotInView(desktopState.allWindows)
        .filterNot { it.isMinimized }
        .map { MinimizeWindow(it.id) }
    val windowToActivate = view.windowToActivate()

    return (minimizeCommands + showCommands + retile(view, desktopState.allWindows, desktopState.layoutSpace))
        .let {
            if (windowToActivate != null) {
                it + ActivateWindow(windowToActivate)
            } else it
        }
}

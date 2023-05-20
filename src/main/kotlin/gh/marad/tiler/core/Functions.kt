package gh.marad.tiler.core

import gh.marad.tiler.core.views.View
import gh.marad.tiler.core.views.ViewManager


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

fun retile(view: View, allWindows: Windows): List<TilerCommand> {
    allWindows.filterNot { it.isMinimized }
        .forEach { view.addWindow(it.id) }
    val windowsInView = view.filterWindowsInView(allWindows)
    view.updateWindowsInView(windowsInView.map { it.id })
    val positionedWindows = view.layout.retile(windowsInView)
    return createPositionCommands(allWindows, positionedWindows)
}

fun activateView(viewId: Int, viewManager: ViewManager, desktopState: DesktopState): List<TilerCommand> {
    val view = viewManager.changeCurrentView(viewId)
    val showCommands = view.filterWindowsInView(desktopState.windows)
        .filter { it.isMinimized }
        .map { ShowWindow(it.id) }
    val minimizeCommands = view.filterWindowsNotInView(desktopState.windows)
        .filterNot { it.isMinimized }
        .map { MinimizeWindow(it.id) }
    val windowToActivate = view.windowToActivate()

    return (minimizeCommands + showCommands + retile(view, desktopState.windows))
        .let {
            if (windowToActivate != null) {
                it + ActivateWindow(windowToActivate)
            } else it
        }
}
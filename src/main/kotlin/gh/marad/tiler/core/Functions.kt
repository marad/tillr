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

fun retile(view: View, desktopState: DesktopState): List<TilerCommand> {
    desktopState.windows.filterNot { it.minimized }
        .forEach { view.addWindow(it.id) }
    val windowsInView = view.filterWindowsInView(desktopState.windows)
    view.updateWindowsInView(windowsInView.map { it.id })
    val positionedWindows = view.layout.retile(windowsInView)
    return createPositionCommands(desktopState.windows, positionedWindows)
}

fun activateView(viewId: Int, viewManager: ViewManager, desktopState: DesktopState): List<TilerCommand> {
    val view = viewManager.changeCurrentView(viewId)
    val showCommands = view.filterWindowsInView(desktopState.windows)
        .filter { it.minimized }
        .map { ShowWindow(it.id) }
    val minimizeCommands = view.filterWindowsNotInView(desktopState.windows)
        .filterNot { it.minimized }
        .map { MinimizeWindow(it.id) }
    val windowToActivate = view.windowToActivate()

    return (minimizeCommands + showCommands + retile(view, desktopState))
        .let {
            if (windowToActivate != null) {
                it + ActivateWindow(windowToActivate)
            } else it
        }
}
package gh.marad.tiler.core

import gh.marad.tiler.core.layout.LayoutSpace
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

fun addGap(windows: Windows, gap: Int): Windows {
    var minX = Int.MAX_VALUE
    var maxX = Int.MIN_VALUE
    var minY = Int.MAX_VALUE
    var maxY = Int.MIN_VALUE
    windows.forEach {
        minX = minX.coerceAtMost(it.position.x)
        maxX = maxX.coerceAtLeast(it.position.x + it.position.width)
        minY = minY.coerceAtMost(it.position.y)
        maxY = maxY.coerceAtLeast(it.position.y + it.position.height)
    }

    val halfGap = gap / 2

    return windows.map {

        val offsetLeft = if (it.position.x == minX) gap else halfGap
        val offsetRight = offsetLeft + if (it.position.x + it.position.width == maxX) gap else halfGap
        val offsetTop = if (it.position.y == minY) gap else halfGap
        val offsetBottom = offsetTop + if (it.position.y + it.position.height == maxY) gap else halfGap

        it.copy(position = it.position.copy(
            x = it.position.x + offsetLeft,
            y = it.position.y + offsetTop,
            width = it.position.width - offsetRight,
            height = it.position.height - offsetBottom
        ))
    }
}
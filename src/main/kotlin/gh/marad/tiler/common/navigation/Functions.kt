package gh.marad.tiler.common.navigation

import gh.marad.tiler.common.Window
import kotlin.math.absoluteValue

fun windowRight(activeWindow: Window, windows: List<Window>): Window? {
    val minXOnTheRight = windows
        .takeWindowsOnTheRightOf(activeWindow)
        .getLeftmostWindow()
        .posXOrNull() ?: return null // there are no candidates
    return windows
        .getWindowsAtX(minXOnTheRight)
        .getWindowNearestToY(activeWindow.position.y)
}

fun windowLeft(activeWindow: Window, windows: List<Window>): Window? {
    val maxXOnTheLeft = windows
        .takeWindowsOnTheLeftOf(activeWindow)
        .getRightmostWindow()
        .posXOrNull() ?: return null
    return windows
        .getWindowsAtX(maxXOnTheLeft)
        .getWindowNearestToY(activeWindow.position.y)
}

fun windowUp(activeWindow: Window, windows: List<Window>): Window? {
    val minYAbove = windows
        .takeWindowsAboveOf(activeWindow)
        .getBottommostWindow()
        .posYOrNull() ?: return null
    return windows
        .getWindowsAtY(minYAbove)
        .getWindowNearestToX(activeWindow.position.x)
}

fun windowDown(activeWindow: Window, windows: List<Window>): Window? {
    val maxYBelow = windows
        .takeWindowsBelowOf(activeWindow)
        .getTopmostWindow()
        .posYOrNull() ?: return null
    return windows
        .getWindowsAtY(maxYBelow)
        .getWindowNearestToX(activeWindow.position.x)
}

private fun List<Window>.takeWindowsOnTheLeftOf(activeWindow: Window) = filter { it.position.x < activeWindow.position.x }
private fun List<Window>.takeWindowsOnTheRightOf(activeWindow: Window) = filter { it.position.x > activeWindow.position.x}
private fun List<Window>.takeWindowsAboveOf(activeWindow: Window) = filter { it.position.y < activeWindow.position.y }
private fun List<Window>.takeWindowsBelowOf(activeWindow: Window) = filter { it.position.y > activeWindow.position.y }

private fun List<Window>.getLeftmostWindow() = minByOrNull { it.position.x }
private fun List<Window>.getRightmostWindow() = maxByOrNull { it.position.x }
private fun List<Window>.getTopmostWindow() = minByOrNull { it.position.y }
private fun List<Window>.getBottommostWindow() = maxByOrNull { it.position.y }

private fun List<Window>.getWindowsAtX(x: Int) = filter { it.position.x == x }
private fun List<Window>.getWindowsAtY(y: Int) = filter { it.position.y == y }

private fun List<Window>.getWindowNearestToX(x: Int) = minByOrNull { (it.position.x - x).absoluteValue }
private fun List<Window>.getWindowNearestToY(y: Int) = minByOrNull { (it.position.y - y).absoluteValue }

private fun Window?.posXOrNull() = this?.position?.x
private fun Window?.posYOrNull() = this?.position?.y


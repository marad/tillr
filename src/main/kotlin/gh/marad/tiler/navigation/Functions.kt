package gh.marad.tiler.navigation

import gh.marad.tiler.winapi.Window
import kotlin.math.absoluteValue

fun windowRight(activeWindow: Window, windows: List<Window>): Window? {
    val minXOnTheRight = windows
        .takeWindowsOnTheRightOf(activeWindow)
        .getLeftmostWindow()
        .posXOrNull() ?: return null // there are no candidates
    return windows
        .getWindowsAtX(minXOnTheRight)
        .getWindowNearestToY(activeWindow.getPos().y)
}

fun windowLeft(activeWindow: Window, windows: List<Window>): Window? {
    val maxXOnTheLeft = windows
        .takeWindowsOnTheLeftOf(activeWindow)
        .getRightmostWindow()
        .posXOrNull() ?: return null
    return windows
        .getWindowsAtX(maxXOnTheLeft)
        .getWindowNearestToY(activeWindow.getPos().y)
}

fun windowUp(activeWindow: Window, windows: List<Window>): Window? {
    val minYAbove = windows
        .takeWindowsAboveOf(activeWindow)
        .getBottommostWindow()
        .posYOrNull() ?: return null
    return windows
        .getWindowsAtY(minYAbove)
        .getWindowNearestToX(activeWindow.getPos().x)
}

fun windowDown(activeWindow: Window, windows: List<Window>): Window? {
    val maxYBelow = windows
        .takeWindowsBelowOf(activeWindow)
        .getTopmostWindow()
        .posYOrNull() ?: return null
    return windows
        .getWindowsAtY(maxYBelow)
        .getWindowNearestToX(activeWindow.getPos().x)
}

private fun List<Window>.takeWindowsOnTheLeftOf(activeWindow: Window) = filter { it.getPos().x < activeWindow.getPos().x }
private fun List<Window>.takeWindowsOnTheRightOf(activeWindow: Window) = filter { it.getPos().x > activeWindow.getPos().x}
private fun List<Window>.takeWindowsAboveOf(activeWindow: Window) = filter { it.getPos().y < activeWindow.getPos().y }
private fun List<Window>.takeWindowsBelowOf(activeWindow: Window) = filter { it.getPos().y > activeWindow.getPos().y }

private fun List<Window>.getLeftmostWindow() = minByOrNull { it.getPos().x }
private fun List<Window>.getRightmostWindow() = maxByOrNull { it.getPos().x }
private fun List<Window>.getTopmostWindow() = minByOrNull { it.getPos().y }
private fun List<Window>.getBottommostWindow() = maxByOrNull { it.getPos().y }

private fun List<Window>.getWindowsAtX(x: Int) = filter { it.getPos().x == x }
private fun List<Window>.getWindowsAtY(y: Int) = filter { it.getPos().y == y }

private fun List<Window>.getWindowNearestToX(x: Int) = minByOrNull { (it.getPos().x - x).absoluteValue }
private fun List<Window>.getWindowNearestToY(y: Int) = minByOrNull { (it.getPos().y - y).absoluteValue }

private fun Window?.posXOrNull() = this?.getPos()?.x
private fun Window?.posYOrNull() = this?.getPos()?.y


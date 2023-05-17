package gh.marad.tiler.navigation

import gh.marad.tiler.winapi.activeWindow
import gh.marad.tiler.winapi.listWindows

fun navigateRight() {
    val activeWindow = activeWindow()
    val windows = listWindows()
}

fun main() {
    navigateRight()
}
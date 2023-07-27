package gh.marad.tiler.os

import gh.marad.tiler.common.Window

interface WindowEventHandler {
    fun windowActivated(window: Window)
    fun windowAppeared(window: Window)
    fun windowDisappeared(window: Window)
    fun windowMinimized(window: Window)
    fun windowRestored(window: Window)
    fun windowMovedOrResized(window: Window)
}
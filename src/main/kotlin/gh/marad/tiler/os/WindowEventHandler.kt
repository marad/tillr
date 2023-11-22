package gh.marad.tiler.os

import gh.marad.tiler.common.Window

interface WindowEventHandler {
    suspend fun windowActivated(window: Window)
    suspend fun windowAppeared(window: Window)
    suspend fun windowDisappeared(window: Window)
    suspend fun windowMinimized(window: Window)
    suspend fun windowRestored(window: Window)
    suspend fun windowMovedOrResized(window: Window)
}
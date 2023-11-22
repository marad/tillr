package gh.marad.tiler.app.internal

import gh.marad.tiler.common.ShowWindow
import gh.marad.tiler.common.Window
import gh.marad.tiler.common.WindowId
import gh.marad.tiler.os.OsFacade
import gh.marad.tiler.os.WindowEventHandler

/// Tracks all the windows and shows hidden ones (the ones on other views) when JVM is being closed.
/// This way you will not end up with hidden windows and now way of showing them.
class RestoreWindowsOnExitEventHandler(os: OsFacade) : WindowEventHandler {
    private val windows = mutableSetOf<WindowId>()
    init {
        val shutdownHook = Thread {
            windows.forEach {
                os.execute(ShowWindow(it))
            }
        }
        Runtime.getRuntime().addShutdownHook(shutdownHook)
    }

    override suspend fun windowActivated(window: Window) {
        windows.add(window.id)
    }

    override suspend fun windowAppeared(window: Window) {
        windows.add(window.id)
    }

    override suspend fun windowDisappeared(window: Window) {
        windows.add(window.id)
    }

    override suspend fun windowMinimized(window: Window) {
        windows.add(window.id)
    }

    override suspend fun windowRestored(window: Window) {
        windows.add(window.id)
    }

    override suspend fun windowMovedOrResized(window: Window) {
        windows.add(window.id)
    }
}
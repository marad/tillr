package gh.marad.tiler.app.internal

import gh.marad.tiler.common.ShowWindow
import gh.marad.tiler.common.Window
import gh.marad.tiler.common.WindowId
import gh.marad.tiler.os.OsFacade
import gh.marad.tiler.os.WindowEventHandler

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

    override fun windowActivated(window: Window) {
        windows.add(window.id)
    }

    override fun windowAppeared(window: Window) {
        windows.add(window.id)
    }

    override fun windowDisappeared(window: Window) {
        windows.add(window.id)
    }

    override fun windowMinimized(window: Window) {
        windows.add(window.id)
    }

    override fun windowRestored(window: Window) {
        windows.add(window.id)
    }

    override fun windowMovedOrResized(window: Window) {
        windows.add(window.id)
    }
}
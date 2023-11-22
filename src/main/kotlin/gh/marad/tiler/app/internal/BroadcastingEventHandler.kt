package gh.marad.tiler.app.internal

import gh.marad.tiler.common.Window
import gh.marad.tiler.os.WindowEventHandler

class BroadcastingEventHandler(private vararg val handlers: WindowEventHandler) : WindowEventHandler{

    override suspend fun windowActivated(window: Window) {
        for (handler in handlers) {
            handler.windowActivated(window)
        }
    }

    override suspend fun windowAppeared(window: Window) {
        for (handler in handlers) {
            handler.windowAppeared(window)
        }
    }

    override suspend fun windowDisappeared(window: Window) {
        for (handler in handlers) {
            handler.windowDisappeared(window)
        }
    }

    override suspend fun windowMinimized(window: Window) {
        for (handler in handlers) {
            handler.windowMinimized(window)
        }
    }

    override suspend fun windowRestored(window: Window) {
        for (handler in handlers) {
            handler.windowRestored(window)
        }
    }

    override suspend fun windowMovedOrResized(window: Window) {
        for (handler in handlers) {
            handler.windowMovedOrResized(window)
        }
    }
}
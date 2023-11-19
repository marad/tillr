package gh.marad.tiler.common

import gh.marad.tiler.os.WindowEventHandler

class BroadcastingEventHandler(private vararg val handlers: WindowEventHandler) : WindowEventHandler{

    override fun windowActivated(window: Window) {
        for (handler in handlers) {
            handler.windowActivated(window)
        }
    }

    override fun windowAppeared(window: Window) {
        for (handler in handlers) {
            handler.windowAppeared(window)
        }
    }

    override fun windowDisappeared(window: Window) {
        for (handler in handlers) {
            handler.windowDisappeared(window)
        }
    }

    override fun windowMinimized(window: Window) {
        for (handler in handlers) {
            handler.windowMinimized(window)
        }
    }

    override fun windowRestored(window: Window) {
        for (handler in handlers) {
            handler.windowRestored(window)
        }
    }

    override fun windowMovedOrResized(window: Window) {
        for (handler in handlers) {
            handler.windowMovedOrResized(window)
        }
    }
}
package gh.marad.tiler.app.internal

import gh.marad.tiler.common.TilerCommand
import gh.marad.tiler.common.filteringrules.FilteringRules
import gh.marad.tiler.os.OsFacade
import gh.marad.tiler.common.Window
import gh.marad.tiler.os.WindowEventHandler
import gh.marad.tiler.tiler.TilerFacade
import kotlinx.coroutines.channels.Channel

class TilerWindowEventHandler(
    private val tiler: TilerFacade,
    private val filteringRules: FilteringRules,
    private val os: OsFacade,
    private val commandChannel: Channel<List<TilerCommand>>
): WindowEventHandler {
    override suspend fun windowActivated(window: Window) {
        if (filteringRules.shouldManage(window)) {
            commandChannel.send(tiler.addWindow(window))
        }
    }

    override suspend fun windowAppeared(window: Window) {
        if (filteringRules.shouldManage(window)) {
            commandChannel.send(tiler.addWindow(window))
        }
    }

    override suspend fun windowDisappeared(window: Window) {
        commandChannel.send(tiler.removeWindow(window))
    }

    override suspend fun windowMinimized(window: Window) {
        commandChannel.send(tiler.removeWindow(window))
    }

    override suspend fun windowRestored(window: Window) {
        if (filteringRules.shouldManage(window)) {
            commandChannel.send(tiler.addWindow(window))
        }
    }

    override suspend fun windowMovedOrResized(window: Window) {
        if (filteringRules.shouldManage(window)) {
            val foundWindow = os.windowsUnderCursor().lastOrNull { it.isVisible }
            if (foundWindow != null && foundWindow.id != window.id) {
                commandChannel.send(tiler.swapWindows(window, foundWindow))
            }
        }
        commandChannel.send(tiler.retile())
    }

}
package gh.marad.tiler.app.internal

import gh.marad.tiler.common.TilerCommand
import gh.marad.tiler.common.filteringrules.FilteringRules
import gh.marad.tiler.os.OsFacade
import gh.marad.tiler.common.Window
import gh.marad.tiler.os.WindowEventHandler
import gh.marad.tiler.tiler.TilerFacade
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking

class TilerWindowEventHandler(
    private val tiler: TilerFacade,
    private val filteringRules: FilteringRules,
    private val os: OsFacade,
    private val commandChannel: Channel<List<TilerCommand>>
): WindowEventHandler {
    override fun windowActivated(window: Window) = runBlocking {
        if (filteringRules.shouldManage(window)) {
            commandChannel.send(tiler.addWindow(window))
        }
    }

    override fun windowAppeared(window: Window) = runBlocking {
        if (filteringRules.shouldManage(window)) {
            commandChannel.send(tiler.addWindow(window))
        }
    }

    override fun windowDisappeared(window: Window) = runBlocking {
        commandChannel.send(tiler.removeWindow(window))
    }

    override fun windowMinimized(window: Window) = runBlocking {
        commandChannel.send(tiler.removeWindow(window))
    }

    override fun windowRestored(window: Window) = runBlocking {
        if (filteringRules.shouldManage(window)) {
            commandChannel.send(tiler.addWindow(window))
        }
    }

    override fun windowMovedOrResized(window: Window) = runBlocking {
        if (filteringRules.shouldManage(window)) {
            val foundWindow = os.windowsUnderCursor().lastOrNull { it.isVisible }
            if (foundWindow != null && foundWindow.id != window.id) {
                commandChannel.send(tiler.swapWindows(window, foundWindow))
            } else {
                commandChannel.send(tiler.retile())
            }
        }
    }

}
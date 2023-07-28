package gh.marad.tiler.os

import gh.marad.tiler.common.DesktopState
import gh.marad.tiler.common.TilerCommand
import gh.marad.tiler.common.Window
import gh.marad.tiler.common.WindowId
import gh.marad.tiler.os.internal.WindowsOs

interface OsFacade {
    fun getDesktopState(): DesktopState
    fun activeWindow(): Window
    fun setActiveWindow(windowId: WindowId)
    fun listWindows(): List<Window>
    fun windowsUnderCursor(): List<Window>
    fun registerHotkey(shortcut: String, handler: () -> Unit): Boolean
    fun clearHotkeys()
    fun execute(command: TilerCommand)
    fun execute(commands: List<TilerCommand>)
    fun startEventHandling(handler: WindowEventHandler)
    fun windowDebugInfo(window: Window): String

    companion object {
        fun createWindowsFacade(): OsFacade = WindowsOs()
    }
}
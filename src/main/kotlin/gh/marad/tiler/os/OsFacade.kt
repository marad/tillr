package gh.marad.tiler.os

import gh.marad.tiler.core.*
import gh.marad.tiler.core.filteringrules.FilteringRules

interface OsFacade {
    fun getDesktopState(filteringRules: FilteringRules): DesktopState
    fun activeWindow(): Window
    fun setActiveWindow(windowId: WindowId)
    fun listWindows(): List<Window>
    fun windowsUnderCursor(): List<Window>
    fun registerHotkey(shortcut: String, handler: () -> Unit): Boolean
    fun execute(command: TilerCommand)
    fun execute(commands: List<TilerCommand>)
    fun startEventHandling(handler: WindowEventHandler)
    fun windowDebugInfo(window: Window): String
}
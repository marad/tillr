package gh.marad.tiler.windowstiler

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser
import gh.marad.tiler.core.*
import gh.marad.tiler.core.filteringrules.FilteringRules
import gh.marad.tiler.core.Window as TilerWindow
import gh.marad.tiler.winapi.*
import gh.marad.tiler.winapi.Window

fun getDesktopState(filteringRules: FilteringRules): DesktopState {
    val space = Monitors.primary().workArea.toLayoutSpace()
    val allWindows = listWindows().map { it.toTilerWindow() }
    val toManage = allWindows.filter { filteringRules.shouldManage(it) }
    return DesktopState(space, allWindows, toManage)
}

fun windowsUnderCursor(): List<TilerWindow> =
    gh.marad.tiler.winapi.windowsUnderCursor()
        .map { it.toTilerWindow() }

fun executeCommand(it: TilerCommand) = when(it) {
    is SetWindowPosition -> {
        val hwnd = (it.windowId as WID).handle
        // correct the window size to account for the invisible
        // border of the window
        val pos = it.position.addInvisibleBorders(windowBorders(hwnd))
        User32.INSTANCE.SetWindowPos(hwnd, null,
            pos.x, pos.y, pos.width, pos.height,
            User32.SWP_ASYNCWINDOWPOS and User32.SWP_NOACTIVATE and User32.SWP_NOZORDER and User32.SWP_SHOWWINDOW)
    }

    is MinimizeWindow -> {
        val hwnd = (it.windowId as WID).handle
        User32.INSTANCE.ShowWindow(hwnd, User32.SW_SHOWMINNOACTIVE)
    }

    is ShowWindow -> {
        val hwnd = (it.windowId as WID).handle
        User32.INSTANCE.ShowWindow(hwnd, User32.SW_SHOWNOACTIVATE)
        User32.INSTANCE.RedrawWindow(hwnd, null, null, WinDef.DWORD(User32.RDW_INVALIDATE.toLong()))
    }

    is ActivateWindow -> {
        val hwnd = (it.windowId as WID).handle
        // sometimes when switching the view window activation would not work
        // retry could be needed, if problem still occurs
        Thread.sleep(100)
        User32.INSTANCE.SetForegroundWindow(hwnd)
    }
}

fun List<TilerCommand>.execute() = forEach { executeCommand(it) }

val ignoredEvents = arrayOf(
    EVENT_OBJECT_LOCATIONCHANGE, EVENT_OBJECT_NAMECHANGE, EVENT_SYSTEM_CAPTURESTART,
    EVENT_SYSTEM_CAPTUREEND, EVENT_OBJECT_DESTROY, EVENT_OBJECT_CREATE,
    EVENT_OBJECT_PARENTCHANGE, EVENT_OBJECT_REORDER
)

fun generateEventProcedure(eventHandler: WindowEventHandler): WinUser.WinEventProc {
    return WinUser.WinEventProc { hWinEventHook, event, hwnd, idObject, idChild, dwEventThread, dwmsEventTime ->
        if (hwnd == null || event in ignoredEvents) return@WinEventProc

        val window = Window(hwnd)

        if(shouldIgnoreEvent(window, event, hwnd, idObject)) {
            return@WinEventProc
        }

        try {
            when (event) {
                EVENT_SYSTEM_FOREGROUND -> eventHandler.windowActivated(window.toTilerWindow()).execute()
                EVENT_OBJECT_FOCUS -> {}
                EVENT_OBJECT_SHOW -> eventHandler.windowAppeared(window.toTilerWindow()).execute()
                EVENT_OBJECT_DESTROY -> eventHandler.windowDisappeared(window.toTilerWindow()).execute()
                EVENT_OBJECT_HIDE -> eventHandler.windowDisappeared(window.toTilerWindow()).execute()
                EVENT_SYSTEM_MINIMIZESTART -> eventHandler.windowMinimized(window.toTilerWindow()).execute()
                EVENT_SYSTEM_MINIMIZEEND -> eventHandler.windowRestored(window.toTilerWindow()).execute()
                EVENT_SYSTEM_MOVESIZESTART -> {}
                EVENT_SYSTEM_MOVESIZEEND -> {
                    eventHandler.windowMovedOrResized(window.toTilerWindow()).execute()
                }

                else -> {}
            }
        } catch (e: Exception) {
            println("Error while handling event $event for window $window")
            e.printStackTrace()
        }
    }
}

fun shouldIgnoreEvent(
    window: Window,
    event: WinDef.DWORD?,
    hwnd: WinDef.HWND,
    idObject: WinDef.LONG?
): Boolean {
    val isNotForWindow = idObject != OBJID_WINDOW
    val isNotAWindow = !window.isWindow()
    val isNotVisibleAndIsNotTryingToHide = !window.isVisible() && event != EVENT_OBJECT_HIDE
    val isCloaked = DwmApi.isCloaked(hwnd)
    val isWindowsTooltip = window.getRealClassName() == "Xaml_WindowedPopupClass"
    val isAToolWindow = window.getExStyle().toolWindow()

    return isNotForWindow ||
            isNotAWindow ||
            isNotVisibleAndIsNotTryingToHide ||
            isCloaked ||
            isWindowsTooltip ||
            isAToolWindow
}

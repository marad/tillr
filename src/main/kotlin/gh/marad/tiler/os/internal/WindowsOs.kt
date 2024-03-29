package gh.marad.tiler.os.internal

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinDef.RECT
import com.sun.jna.platform.win32.WinUser.WINDOWPLACEMENT
import gh.marad.tiler.common.*
import gh.marad.tiler.common.Window
import gh.marad.tiler.os.OsFacade
import gh.marad.tiler.os.WindowEventHandler
import gh.marad.tiler.os.internal.winapi.*
import gh.marad.tiler.os.internal.winapi.Window as OsWindow

class WindowsOs : OsFacade {
    companion object {
        private val hotkeys = Hotkeys()
    }
//    private val myU32 = Native.load("user32", MyUser32::class.java, W32APIOptions.DEFAULT_OPTIONS)

    override fun getDesktopState(): DesktopState {
        val monitors = Monitors.list().map { Monitor(it.workArea.toLayoutSpace(), it.isPrimary) }
        val windows = gh.marad.tiler.os.internal.winapi.listWindows().mapNotNull {
            if (it.isWindow()) {
                it.toTilerWindow()
            } else null
        }
        return DesktopState(monitors, windows)
    }

    override fun activeWindow(): Window =
        gh.marad.tiler.os.internal.winapi.activeWindow().toTilerWindow()

    override fun setActiveWindow(windowId: WindowId) {
        val id = windowId as WID
        User32.INSTANCE.SetForegroundWindow(id.handle)
    }

    override fun listWindows(): List<Window> {
        return gh.marad.tiler.os.internal.winapi.listWindows().map { it.toTilerWindow() }
    }

    override fun windowsUnderCursor(): List<Window> {
        return gh.marad.tiler.os.internal.winapi.windowsUnderCursor()
            .map { it.toTilerWindow() }
    }

    override fun registerHotkey(shortcut: String, handler: () -> Unit): Boolean {
        return hotkeys.register(shortcut, handler)
    }

    override fun clearHotkeys() {
        hotkeys.clear()
    }

    override fun execute(command: TilerCommand) {
        when(command) {
            is SetWindowPosition -> {
                val hwnd = (command.windowId as WID).handle
                // correct the window size to account for the invisible
                // border of the window
                val pos = command.position.addInvisibleBorders(windowBorders(hwnd))
                val placement = WINDOWPLACEMENT()
                placement.length = placement.size()
                placement.showCmd = User32.SW_SHOWNOACTIVATE and User32.SWP_NOZORDER
                val r = RECT()
                r.left = pos.x
                r.top = pos.y
                r.right = pos.x + pos.width
                r.bottom = pos.y + pos.height
                placement.rcNormalPosition = r
                User32.INSTANCE.SetWindowPlacement(hwnd, placement)
            }

            is HideWindow -> {
                val hwnd = (command.windowId as WID).handle
                User32.INSTANCE.ShowWindow(hwnd, User32.SW_HIDE)
            }

            is ShowWindow -> {
                val hwnd = (command.windowId as WID).handle
                User32.INSTANCE.ShowWindow(hwnd, User32.SW_SHOWNOACTIVATE)
                User32.INSTANCE.RedrawWindow(hwnd, null, null, WinDef.DWORD(User32.RDW_INVALIDATE.toLong()))
            }

            is ActivateWindow -> {
                val hwnd = (command.windowId as WID).handle
                // sometimes when switching the view window activation would not work
                // retry could be needed, if problem still occurs
                Thread.sleep(100)
                User32.INSTANCE.SetForegroundWindow(hwnd)
            }
        }
    }

    override fun execute(commands: List<TilerCommand>) {
//        var deferStruct = myU32.BeginDeferWindowPos(commands.count { it is SetWindowPosition })
        commands.forEach {
//            if (it is SetWindowPosition) {
//                val hwnd = (it.windowId as WID).handle
//                val pos = it.position.addInvisibleBorders(windowBorders(hwnd))
//                deferStruct = myU32.DeferWindowPos(deferStruct, hwnd, null,
//                    pos.x, pos.y, pos.width, pos.height,
//                    User32.SWP_ASYNCWINDOWPOS and User32.SWP_NOACTIVATE and User32.SWP_NOZORDER and User32.SWP_SHOWWINDOW
//                )
//            } else {
                execute(it)
//            }
        }
//        Thread.sleep(50)
//        myU32.EndDeferWindowPos(deferStruct)
    }

    override fun startEventHandling(handler: WindowEventHandler) {
        val tilerProc = generateEventProcedure(handler)
        User32.INSTANCE.SetWinEventHook(EVENT_MIN, EVENT_MAX, null, tilerProc, 0, 0, 0)
        windowsMainLoop()
    }

    override fun isWindowAtPosition(windowId: WindowId, position: WindowPosition): Boolean {
        val hwnd = (windowId as WID).handle
        val placement = WINDOWPLACEMENT()
        User32.INSTANCE.GetWindowPlacement(hwnd, placement)
        val actualPosition = placement.rcNormalPosition
        val targetPosition = position.addInvisibleBorders(windowBorders(hwnd))

        return targetPosition.left == actualPosition.left &&
                targetPosition.top == actualPosition.top
    }

    override fun userHome(): String =
        System.getenv("userprofile")

    override fun windowDebugInfo(window: Window): String {
        val wid = window.id as WID
        val osWindow = OsWindow(wid.handle)

        val sb = StringBuilder()
        sb.appendLine("Parent: ${User32.INSTANCE.GetParent(wid.handle)}")
        sb.appendLine("Name: ${osWindow.getTitle()}")
        sb.appendLine("Class: ${osWindow.getRealClassName()}")
        sb.appendLine("Process: ${osWindow.getProcess().exePath()}")
        sb.appendLine("Pos: ${osWindow.getPos()}")
        sb.appendLine("Minimized: ${osWindow.isMinimized()}")
        sb.appendLine("Maximized: ${osWindow.isMaximized()}")
        sb.appendLine("Visible: ${osWindow.isVisible()}")
        sb.appendLine("Popup: ${osWindow.isPopup()}")
        sb.appendLine("Visible frame border thickness: ${DwmApi.getVisibleFrameBorderThickness(wid.handle)}")
        sb.appendLine("Window borders: ${windowBorders(wid.handle)}")
        sb.appendLine("Style / caption: ${osWindow.getStyle().caption()}")
        sb.appendLine("Style / border: ${osWindow.getStyle().border()}")
        sb.appendLine("Style / sysMenu: ${osWindow.getStyle().sysMenu()}")
        sb.appendLine("ExStyle / clientEdge: ${osWindow.getExStyle().clientEdge()}")
        sb.appendLine("ExStyle / appWindow: ${osWindow.getExStyle().appWindow()}")
        sb.appendLine("ExStyle / windowEdge: ${osWindow.getExStyle().windowEdge()}")
        sb.appendLine("SM_CYCAPTION: ${User32.INSTANCE.GetSystemMetrics(User32.SM_CYCAPTION)}")
        return sb.toString()
    }
}
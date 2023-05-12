import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser.WinEventProc
import gh.marad.tiler.core.*
import gh.marad.tiler.winapi.*
import gh.marad.tiler.winapi.Window

data class WID(val handle: WinDef.HWND): WindowId

val overrides = listOf(
    ManageOverride(true) { win -> win.getTitle() in listOf("WhatsApp", "Messenger") },
    ManageOverride(true) { win -> win.getTitle() == "Microsoft To Do" && win.getRealClassName() == "ApplicationFrameWindow"},
    ManageOverride(false) { win -> win.getRealClassName() == "ApplicationFrameTitleBarWindow" },
    ManageOverride(false) { win -> win.getTitle().isBlank() && win.getProcess().exeName() == "idea64.exe"}
)

fun List<ManageOverride>.shouldManage(win: Window): Boolean =
    filter { it.matcher.invoke(win) }
        .any { it.shouldManage }

fun List<ManageOverride>.shouldNotManage(win: Window): Boolean =
    filterNot { it.shouldManage }
        .any { it.matcher.invoke(win) }

fun shouldManage(window: Window) = !window.getStyle().popupWindow() || overrides.shouldManage(window)

fun getDesktopState(): DesktopState {
    return DesktopState(
        listWindows()
            .filter { shouldManage(it) }
            .map { it.toTilerWindow() }
    )
}


fun executeCommand(it: TilerCommand) = when(it) {
    is SetWindowPosition -> {
        val hwnd = (it.windowId as WID).handle
        // correct the window size to account for the invisible
        // border of the window
        val pos = it.position.addInvisibleBorders()
//        User32.INSTANCE.ShowWindow(hwnd, User32.SW_NORMAL)
        User32.INSTANCE.SetWindowPos(hwnd, null,
            pos.x, pos.y, pos.width, pos.height,
            User32.SWP_ASYNCWINDOWPOS and User32.SWP_NOACTIVATE and User32.SWP_NOZORDER and User32.SWP_SHOWWINDOW)
    }

    is MinimizeWindow -> {
        val hwnd = (it.windowId as WID).handle
        User32.INSTANCE.ShowWindow(hwnd, User32.SW_SHOWMINNOACTIVE)
    }
}

fun List<TilerCommand>.execute() = forEach { executeCommand(it) }

val ignoredEvents = arrayOf(
    EVENT_OBJECT_LOCATIONCHANGE, EVENT_OBJECT_NAMECHANGE, EVENT_SYSTEM_CAPTURESTART,
    EVENT_SYSTEM_CAPTUREEND, EVENT_OBJECT_DESTROY, EVENT_OBJECT_CREATE, EVENT_OBJECT_PARENTCHANGE, EVENT_OBJECT_REORDER
)

fun generateEventProcedure(tiler: Tiler): WinEventProc {
    return WinEventProc { hWinEventHook, event, hwnd, idObject, idChild, dwEventThread, dwmsEventTime ->
        if (hwnd == null || event in ignoredEvents) return@WinEventProc

        val window = Window(hwnd)

        // handle only window events
        if (idObject != OBJID_WINDOW) return@WinEventProc

        // ignore events about windows tooltip windows
        if (window.getRealClassName() == "Xaml_WindowedPopupClass") return@WinEventProc

        // window should be visible unless it's trying to tell us it's hiding
        val isNotVisible = !window.isVisible() && event != EVENT_OBJECT_HIDE
        if (isNotVisible || DwmApi.isCloaked(hwnd) || !window.isWindow() || window.getExStyle().toolWindow()) {
            return@WinEventProc
        }

        // handle only events for windows in view and events that introduce new windows into current view
        val shouldHandle = (tiler.inView(WID(hwnd)) || event in arrayOf(EVENT_OBJECT_SHOW, EVENT_SYSTEM_MINIMIZEEND)) || overrides.shouldManage(window)
        if (!shouldHandle) {
            return@WinEventProc
        }

        if(overrides.shouldNotManage(window)) return@WinEventProc

        println("0x${Integer.toHexString(event.toInt())} $hwnd - ${window.getTitle()} ")
        println(window.getProcess().exePath())
        println("${window.getClassName()}, ${window.getStyle().caption()}, ${window.getExStyle().appWindow()}")

        val commands: List<TilerCommand> = when (event) {
            EVENT_SYSTEM_FOREGROUND -> emptyList()
            EVENT_OBJECT_FOCUS -> emptyList()
            EVENT_OBJECT_SHOW -> tiler.windowAppeared(window.toTilerWindow())
            EVENT_OBJECT_HIDE -> tiler.windowDisappeared(window.toTilerWindow())
            EVENT_SYSTEM_MINIMIZESTART -> tiler.windowMinimized(window.toTilerWindow())
            EVENT_SYSTEM_MINIMIZEEND -> tiler.windowRestored(window.toTilerWindow())
            EVENT_SYSTEM_MOVESIZESTART -> emptyList()
            EVENT_SYSTEM_MOVESIZEEND -> {
                val foundWindow = windowsUnderCursor().lastOrNull()
                if (foundWindow != null && foundWindow.handle != window.handle) {
                    tiler.swapWindows(window.toTilerWindow().id, foundWindow.toTilerWindow().id)
                }
                tiler.retile()
            }
            else -> emptyList()
        }
        println(commands)
        println("-------------------")
        commands.execute()
    }
}

fun main(args: Array<String>) {
    val monitor = Monitors.primary()

    val layout = TwoColumnLayout(monitor.workArea.toLayoutSpace())
//    val layout = VerticalStackLayout(monitor.workArea.toLayoutSpace())
    val tiler = Tiler(layout, ::getDesktopState)
    tiler.activateView(0)
    getDesktopState().windows.forEach {
        if (!it.minimized) {
            tiler.windowAppeared(it)
            println("${it.id} - ${it.windowName}")
        }
    }
    tiler.retile().execute()

//    @Suppress("UNUSED_VARIABLE") // to avoid GC
//    val watcherHook = startEventWatcher()
    val tilerProc = generateEventProcedure(tiler)
    User32.INSTANCE.SetWinEventHook(EVENT_MIN, EVENT_MAX, null, tilerProc, 0, 0, 0)
    windowsMainLoop()
}
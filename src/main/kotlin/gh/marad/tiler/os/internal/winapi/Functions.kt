package gh.marad.tiler.os.internal.winapi

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinDef.POINT
import com.sun.jna.platform.win32.WinUser.MSG
import com.sun.jna.platform.win32.WinUser.WM_QUIT
import com.sun.jna.win32.W32APIOptions
import java.awt.Point

private val u32 = User32.INSTANCE
private val myU32 = Native.load("user32", MyUser32::class.java, W32APIOptions.DEFAULT_OPTIONS)


fun activeWindow(): Window = Window(u32.GetForegroundWindow())

fun listWindows(): List<Window> {
    val windows = mutableListOf<Window>()
    u32.EnumWindows({ handle: HWND, data: Pointer? ->
        val win = Window(handle)
        if (isInterestingWindow(win)) {
            windows.add(win)
        }
        true
    }, null)
    return windows
}

 fun isInterestingWindow(win: Window): Boolean {
     val isNotCloaked = !DwmApi.isCloaked(win.handle)
     val isNotAToolWindow = !win.getExStyle().toolWindow()
     val isNotATaskManager = win.getRealClassName() != "TaskManagerWindow"
     val isNotAWindowsTooltop = win.getRealClassName() != "Xaml_WindowedPopupClass"

     return isNotAToolWindow
             && isNotCloaked
             && isNotATaskManager
             && isNotAWindowsTooltop
             && win.isWindow()
             && win.getTitle().isNotEmpty()
 }

fun windowsUnderCursor(): List<Window> {
    val cursor = POINT()
    u32.GetCursorPos(cursor)
    val windows = listWindows()
    return windows
        .filterNot { it.isMinimized() }
        .filter {
            it.getPos().contains(Point(cursor.x, cursor.y))
        }
}

fun windowsMainLoop() {
    val msg = MSG()
    while (u32.GetMessage(msg, null, 0, 0) != WM_QUIT) {
        u32.TranslateMessage(msg)
        u32.DispatchMessage(msg)
    }
}

fun windowBorders(hwnd: HWND): WindowBorders {
    val clientRect = WinDef.RECT()
    User32.INSTANCE.GetClientRect(hwnd, clientRect)
    val windowRect = WinDef.RECT()
    User32.INSTANCE.GetWindowRect(hwnd, windowRect)
    val topLeft = POINT(0, 0)
    val bottomRight = POINT(clientRect.toRectangle().width, clientRect.toRectangle().height)
    myU32.ClientToScreen(hwnd, topLeft)
    myU32.ClientToScreen(hwnd, bottomRight)
    return WindowBorders(
        topLeft.x - windowRect.left - 1,
        topLeft.y - windowRect.top,
        windowRect.right - bottomRight.x - 1,
        windowRect.bottom - bottomRight.y - 1)
}
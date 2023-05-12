package gh.marad.tiler.winapi

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinDef.POINT
import com.sun.jna.platform.win32.WinUser.MSG
import com.sun.jna.platform.win32.WinUser.WM_QUIT
import java.awt.Point

private val u32 = User32.INSTANCE


typealias WindowMatcher = (Window) -> Boolean

data class ManageOverride(val shouldManage: Boolean, val matcher: WindowMatcher)


fun listWindows(): List<Window> {
    val windows = mutableListOf<Window>()
    u32.EnumWindows({ handle: HWND, data: Pointer? ->
        val win = Window(handle)
        val isNotCloaked = !DwmApi.isCloaked(handle)
        val isNotAToolWindow = !win.getExStyle().toolWindow()

        if (
            isNotAToolWindow
            && isNotCloaked
            && win.isWindow()
            && win.isVisible()
        ) {
            windows.add(win)
        }
        true
    }, null)
    return windows
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


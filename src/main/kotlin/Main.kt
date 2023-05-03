import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.win32.W32APIOptions

val u32 = User32.INSTANCE;
val myU32 = Native.load("user32", MyUser32::class.java, W32APIOptions.DEFAULT_OPTIONS);

interface MyUser32 : StdCallLibrary, WinUser, WinNT  {
    fun IsIconic(hwnd: HWND?): Boolean
    fun IsZoomed(hwnd: HWND?): Boolean
}

fun showWindows(window: WinDef.HWND, data: Pointer?): Boolean {
    val isVisible = u32.IsWindowVisible(window)
    if (isVisible) {
        val len = u32.GetWindowTextLength(window) + 1
        val nameArray = CharArray(len)
        u32.GetWindowText(window, nameArray, len)
        println("Window name: ${nameArray.concatToString()}")
    }
    return true;
}

data class Window(val handle: HWND) {
    fun getText(): String {
        val len = u32.GetWindowTextLength(handle) + 1
        val nameArray = CharArray(len)
        u32.GetWindowText(handle, nameArray, len)
        return nameArray
            .concatToString()
            .trim(Char(0)) // remove NULL terminator
    }

    fun isMinimized(): Boolean = myU32.IsIconic(handle)
    fun isMaximized(): Boolean = myU32.IsZoomed(handle)
    fun isVisible(): Boolean = u32.IsWindowVisible(handle)
}

fun collectVisibleWindows(): List<Window> {
    val windows = mutableListOf<Window>()
    u32.EnumWindows({ handle: HWND, data: Pointer? ->
        val win = Window(handle)
        if (win.isVisible() && !win.isMinimized() && win.getText().isNotBlank()) {
            windows.add(Window(handle))
        }
        true
    }, null)
    return windows
}

fun main(args: Array<String>) {

    collectVisibleWindows().map {
        println(it.getText())
    }
}
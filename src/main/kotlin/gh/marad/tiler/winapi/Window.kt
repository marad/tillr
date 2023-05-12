package gh.marad.tiler.winapi

import com.sun.jna.Native
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.W32APIOptions
import java.awt.Rectangle

private val u32 = User32.INSTANCE
private val myU32 = Native.load("user32", MyUser32::class.java, W32APIOptions.DEFAULT_OPTIONS)

data class Window(val handle: WinDef.HWND) {
    fun getTitle(): String {
        val len = u32.GetWindowTextLength(handle) + 1
        val nameArray = CharArray(len)
        u32.GetWindowText(handle, nameArray, len)
        return nameArray
            .concatToString()
            .trim(Char(0)) // remove NULL terminator
    }

    fun isWindow(): Boolean = u32.IsWindow(handle)

    fun getClassName(): String {
        val classNameArray = CharArray(257)
        u32.GetClassName(handle, classNameArray, 257)
        return classNameArray
            .concatToString()
            .trim(Char(0)) // remove NULL terminator
    }

    fun getRealClassName(): String {
        val classNameArray = CharArray(257)
        myU32.RealGetWindowClassW(handle, classNameArray, WinDef.UINT(257))
        return classNameArray
            .concatToString()
            .trim(Char(0)) // remove NULl terminator
    }

    fun getPos(): Rectangle {
        val rect = WinDef.RECT()
        if (!u32.GetWindowRect(handle, rect)) {
            System.err.println("Couldn't get window $handle position")
        }
        return rect.toRectangle()
    }

    fun isMinimized(): Boolean = myU32.IsIconic(handle)
    fun isMaximized(): Boolean = myU32.IsZoomed(handle)
    fun isVisible(): Boolean = u32.IsWindowVisible(handle)

    fun getStyle(): Style = Style(u32.GetWindowLong(handle, WinUser.GWL_STYLE))
    fun getExStyle(): ExStyle = ExStyle(u32.GetWindowLong(handle, WinUser.GWL_EXSTYLE))

    fun getProcess(): Process {
        val processId = IntByReference()
        myU32.GetWindowThreadProcessId(handle, processId)
        return Process(processId.value)
    }



}
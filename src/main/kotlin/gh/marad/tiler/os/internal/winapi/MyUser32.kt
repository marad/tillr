
package gh.marad.tiler.os.internal.winapi

import com.sun.jna.platform.win32.WinDef.*
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.StdCallLibrary

@Suppress("unused", "FunctionName")
interface MyUser32 : StdCallLibrary, WinUser, WinNT {
    fun IsIconic(hwnd: HWND?): Boolean
    fun IsZoomed(hwnd: HWND?): Boolean
    fun RealGetWindowClassW(handle: HWND, className: CharArray, maxNameLength: UINT): UINT
    fun GetWindowThreadProcessId(hwnd: HWND, handle: IntByReference): DWORD

    fun ScreenToClient(hwnd: HWND?, point: POINT?)
    fun ClientToScreen(hwnd: HWND?, point: POINT?)

    fun AnimateWindow(hwnd: HWND?, dwTime: DWORD, dwFlags: DWORD): Boolean
}
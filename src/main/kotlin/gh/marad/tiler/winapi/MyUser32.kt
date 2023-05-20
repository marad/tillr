
package gh.marad.tiler.winapi

import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.StdCallLibrary

@Suppress("unused")
interface MyUser32 : StdCallLibrary, WinUser, WinNT {
    fun IsIconic(hwnd: WinDef.HWND?): Boolean
    fun IsZoomed(hwnd: WinDef.HWND?): Boolean
    fun RealGetWindowClassW(handle: WinDef.HWND, className: CharArray, maxNameLength: WinDef.UINT): WinDef.UINT
    fun GetWindowThreadProcessId(hwnd: WinDef.HWND, handle: IntByReference): WinDef.DWORD

    fun ScreenToClient(hwnd: WinDef.HWND?, point: WinDef.POINT?)
    fun ClientToScreen(hwnd: WinDef.HWND?, point: WinDef.POINT?)
}
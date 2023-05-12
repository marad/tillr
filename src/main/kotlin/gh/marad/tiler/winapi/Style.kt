package gh.marad.tiler.winapi

import com.sun.jna.platform.win32.WinUser

class Style(val style: Int) {
    fun border() = check(WinUser.WS_BORDER)
    fun caption() = check(WinUser.WS_CAPTION)
    fun child() = check(WinUser.WS_CHILD)
    fun dlgFrame() = check(WinUser.WS_DLGFRAME)
    fun popupWindow() = check(WinUser.WS_POPUP)
    private fun check(flag: Int): Boolean = (style and flag) == flag
}
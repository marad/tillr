package gh.marad.tiler.winapi

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.W32APIOptions

interface DwmApi : Library {
    fun DwmIsCompositionEnabled(enabled: WinDef.BOOLByReference)
    fun DwmGetWindowAttribute(handle: WinDef.HWND, attribute: WinDef.DWORD, data: Pointer, length: WinDef.DWORD)

    companion object {
        val INSTANCE = Native.load("dwmapi", DwmApi::class.java, W32APIOptions.DEFAULT_OPTIONS)

        fun isCompositionEnabled(): Boolean {
            val enabled = WinDef.BOOLByReference()
            INSTANCE.DwmIsCompositionEnabled(enabled)
            return enabled.value.booleanValue()
        }

        val DWMA_CLOAK = WinDef.DWORD(13)
        val DWMA_CLOAKED = WinDef.DWORD(14)

        val DWM_CLOAKED_APP = 0x0000001
        val DWM_CLOAKED_SHELL = 0x0000002
        val DWM_CLOAKED_INHERITED = 0x0000004

        fun isCloaked(handle: WinDef.HWND): Boolean {
            val result = IntByReference()
            INSTANCE.DwmGetWindowAttribute(handle, DWMA_CLOAKED, result.pointer, WinDef.DWORD(Int.SIZE_BYTES.toLong()))
            return result.value != 0
        }
    }
}
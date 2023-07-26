package gh.marad.tiler.os.internal.winapi

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.WinDef.*
import com.sun.jna.ptr.IntByReference
import com.sun.jna.win32.W32APIOptions

@Suppress("FunctionName", "MemberVisibilityCanBePrivate", "SpellCheckingInspection")
interface DwmApi : Library {
    fun DwmIsCompositionEnabled(enabled: BOOLByReference)
    fun DwmGetWindowAttribute(handle: HWND, attribute: DWORD, data: Pointer, length: DWORD)

    @Suppress("unused")
    companion object {
        private val INSTANCE: DwmApi = Native.load("dwmapi", DwmApi::class.java, W32APIOptions.DEFAULT_OPTIONS)

        fun isCompositionEnabled(): Boolean {
            val enabled = BOOLByReference()
            INSTANCE.DwmIsCompositionEnabled(enabled)
            return enabled.value.booleanValue()
        }

        val DWMA_CLOAK = DWORD(13)
        val DWMA_CLOAKED = DWORD(14)
        val DWMWA_EXTENDED_FRAME_BOUNDS = DWORD(9)
        val DWMWA_CAPTION_BUTTON_BOUNDS = DWORD(5)
        val DWMWA_VISIBLE_FRAME_BORDER_THICKNESS = DWORD(12)

        const val DWM_CLOAKED_APP = 0x0000001
        const val DWM_CLOAKED_SHELL = 0x0000002
        const val DWM_CLOAKED_INHERITED = 0x0000004

        fun isCloaked(handle: HWND): Boolean {
            val result = IntByReference()
            INSTANCE.DwmGetWindowAttribute(handle, DWMA_CLOAKED, result.pointer, DWORD(Int.SIZE_BYTES.toLong()))
            return result.value != 0
        }

        fun getExtendedFrameBounds(handle: HWND): RECT {
            val rect = RECT()
            INSTANCE.DwmGetWindowAttribute(handle, DWMWA_EXTENDED_FRAME_BOUNDS, rect.pointer, DWORD(rect.size().toLong()))
            return rect
        }

        fun getVisibleFrameBorderThickness(handle: HWND): Int {
            val thickness = UINTByReference()
            INSTANCE.DwmGetWindowAttribute(handle, DWMWA_VISIBLE_FRAME_BORDER_THICKNESS, thickness.pointer, DWORD(UINT.SIZE.toLong()))
            return thickness.value.toInt()
        }

    }
}
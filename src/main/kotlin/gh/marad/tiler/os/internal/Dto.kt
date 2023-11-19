package gh.marad.tiler.os.internal

import com.sun.jna.platform.win32.WinDef
import gh.marad.tiler.common.WindowId
import gh.marad.tiler.common.WindowPosition
import gh.marad.tiler.common.layout.LayoutSpace
import gh.marad.tiler.os.internal.winapi.WindowBorders
import gh.marad.tiler.os.internal.winapi.windowBorders
import java.awt.Rectangle
import gh.marad.tiler.common.Window as TilerWindow
import gh.marad.tiler.os.internal.winapi.Window as OsWindow

data class WID(val handle: WinDef.HWND): WindowId

fun Rectangle.toPosition(): WindowPosition = WindowPosition(x, y, width, height)
fun WindowPosition.addInvisibleBorders(windowBorders: WindowBorders) = WindowPosition(x-windowBorders.left, y, width+windowBorders.right + windowBorders.left, height + windowBorders.bottom)
fun WindowPosition.removeInvisibleBorders(windowBorders: WindowBorders) = WindowPosition(x+windowBorders.left, y, width - windowBorders.right - windowBorders.left, height - windowBorders.bottom)

fun Rectangle.toLayoutSpace(): LayoutSpace = LayoutSpace(x, y, width, height)

fun OsWindow.toTilerWindow() = TilerWindow(
    WID(handle),
    getTitle(),
    getRealClassName(),
    try { getProcess().exePath() } catch (e: Exception) { "" },
    getPos().toPosition().removeInvisibleBorders(windowBorders(handle)),
    isMinimized(),
    isMaximized(),
    isPopup(),
    isActive(),
    isVisible()
)

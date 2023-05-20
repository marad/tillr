package gh.marad.tiler.windowstiler

import com.sun.jna.platform.win32.WinDef
import gh.marad.tiler.core.*
import gh.marad.tiler.core.layout.LayoutSpace
import java.awt.Rectangle
import gh.marad.tiler.core.Window as TilerWindow
import gh.marad.tiler.winapi.Window as OsWindow

data class WID(val handle: WinDef.HWND): WindowId

fun Rectangle.toPosition(): WindowPosition = WindowPosition(x, y, width, height)
fun WindowPosition.addInvisibleBorders() = WindowPosition(x-7, y, width+14, height+7)
fun WindowPosition.removeInvisibleBorders() = WindowPosition(x+7, y, width-14, height-7)
fun Rectangle.toLayoutSpace(): LayoutSpace = LayoutSpace(x, y, width, height)

fun OsWindow.toTilerWindow() = TilerWindow(
    WID(handle),
    getTitle(),
    getRealClassName(),
    getProcess().exePath(),
    getPos().toPosition().removeInvisibleBorders(),
    isMinimized()
)

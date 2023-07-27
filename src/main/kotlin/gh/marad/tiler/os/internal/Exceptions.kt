package gh.marad.tiler.os.internal

import com.sun.jna.platform.win32.WinDef.HWND

class CannotGetWindowPositionException(handle: HWND) : RuntimeException("Couldn't get window $handle position")
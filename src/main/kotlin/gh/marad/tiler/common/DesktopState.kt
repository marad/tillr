package gh.marad.tiler.common

import gh.marad.tiler.common.layout.LayoutSpace

data class DesktopState(
    val layoutSpace: LayoutSpace,
    val allWindows: Windows,
    val windowsToManage: Windows,
)
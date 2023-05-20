package gh.marad.tiler.winapi

class ExStyle(val exStyle: Int) {
    fun acceptFiles() = check(WS_EX_ACCEPTFILES)
    fun appWindow() = check(WS_EX_APPWINDOW)
    fun clientEdge() = check(WS_EX_CLIENTEDGE)
    fun composited() = check(WS_EX_COMPOSITED)
    fun dlgModalFrame() = check(WS_EX_DLGMODALFRAME)
    fun mdiChild() = check(WS_EX_MDICHILD)
    fun noActivate() = check(WS_EX_NOACTIVATE)
    fun toolWindow() = check(WS_EX_TOOLWINDOW)
    fun topmost() = check(WS_EX_TOPMOST)
    fun windowEdge() = check(WS_EX_WINDOWEDGE)
    private fun check(flag: Int): Boolean = (exStyle and flag) == flag
}
package gh.marad.tiler.common.assignments

import gh.marad.tiler.common.Window

object Assign {
    fun viewToWindow(viewId: Int, matcher: (Window) -> Boolean) = Assignment(viewId, matcher)
}
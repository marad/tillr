package gh.marad.tiler.common.assignments

import gh.marad.tiler.common.Window

data class Assignment internal constructor(val viewId: Int, val matcher: (Window) -> Boolean) {
    val matches: (Window) -> Boolean = matcher
}

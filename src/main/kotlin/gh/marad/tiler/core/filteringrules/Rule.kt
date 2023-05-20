package gh.marad.tiler.core.filteringrules

import gh.marad.tiler.core.Window

data class Rule internal constructor(val shouldManage: Boolean, val matcher: (Window) -> Boolean) {

    fun matches(window: Window) = matcher(window)

    companion object {
        fun manageIf(matcher: (Window) -> Boolean) = Rule(true, matcher)
        fun ignoreIf(matcher: (Window) -> Boolean) = Rule(false, matcher)
    }
}

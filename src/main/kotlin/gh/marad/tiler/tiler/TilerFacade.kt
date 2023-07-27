package gh.marad.tiler.tiler

import gh.marad.tiler.common.TilerCommand
import gh.marad.tiler.common.Window
import gh.marad.tiler.common.filteringrules.FilteringRules
import gh.marad.tiler.common.layout.Layout
import gh.marad.tiler.os.OsFacade
import gh.marad.tiler.tiler.internal.WindowsTiler
import gh.marad.tiler.tiler.internal.views.ViewManager

interface TilerFacade {
    var enabled: Boolean

    fun initializeWithOpenWindows(): List<TilerCommand>
    fun switchToView(viewId: Int): List<TilerCommand>
    fun switchToPreviousView(): List<TilerCommand>
    fun addWindow(window: Window): List<TilerCommand>
    fun removeWindow(window: Window): List<TilerCommand>
    fun moveWindow(window: Window, viewId: Int): List<TilerCommand>
    fun swapWindows(first: Window, second: Window): List<TilerCommand>
    fun retile(): List<TilerCommand>

    companion object {
        fun windowsTiler(layout: Layout, filteringRules: FilteringRules, os: OsFacade): TilerFacade {
            return WindowsTiler(ViewManager { layout }, filteringRules, os)
        }
    }
}
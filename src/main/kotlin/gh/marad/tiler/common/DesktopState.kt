package gh.marad.tiler.common

import gh.marad.tiler.common.filteringrules.FilteringRules
import gh.marad.tiler.common.layout.LayoutSpace

data class DesktopState(
    val monitors: List<Monitor>,
    val windows: Windows,
) {
    fun getManagableWindows(filteringRules: FilteringRules): Windows = windows.filter { filteringRules.shouldManage(it) }
}

data class Monitor(
    val layoutSpace: LayoutSpace,
    val isPrimary: Boolean
)
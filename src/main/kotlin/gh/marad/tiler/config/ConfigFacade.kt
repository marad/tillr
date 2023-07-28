package gh.marad.tiler.config

import gh.marad.tiler.common.assignments.WindowAssignments
import gh.marad.tiler.common.filteringrules.FilteringRules
import gh.marad.tiler.common.layout.Layout
import gh.marad.tiler.config.internal.SimpleConfig

interface ConfigFacade {
    fun reload()
    fun createLayout(): Layout
    fun getHotkeys(): List<Hotkey>
    fun getFilteringRules(): FilteringRules
    fun getAssignments(): WindowAssignments

    companion object {
        fun createConfig(): ConfigFacade = SimpleConfig()
    }
}
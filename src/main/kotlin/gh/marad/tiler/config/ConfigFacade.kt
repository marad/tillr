package gh.marad.tiler.config

import gh.marad.tiler.common.filteringrules.Rule
import gh.marad.tiler.common.layout.Layout
import gh.marad.tiler.config.internal.SimpleConfig

interface ConfigFacade {
    fun createLayout(): Layout
    fun getHotkeys(): List<Hotkey>
    fun getRules(): List<Rule>

    companion object {
        fun createConfig(): ConfigFacade = SimpleConfig()
    }
}
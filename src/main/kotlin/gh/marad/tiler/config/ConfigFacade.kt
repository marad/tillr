package gh.marad.tiler.config

import gh.marad.tiler.common.layout.Layout
import gh.marad.tiler.config.internal.SimpleConfig

interface ConfigFacade {
    fun createLayout(): Layout
    fun getHotkeys(): List<Hotkey>

    companion object {
        fun createConfig(): ConfigFacade = SimpleConfig()
    }
}
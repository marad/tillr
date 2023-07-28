package gh.marad.tiler.app

import gh.marad.tiler.actions.ActionsFacade
import gh.marad.tiler.app.internal.App
import gh.marad.tiler.common.filteringrules.FilteringRules
import gh.marad.tiler.config.ConfigFacade
import gh.marad.tiler.os.OsFacade
import gh.marad.tiler.tiler.TilerFacade

interface AppFacade {
    fun start(filteringRules: FilteringRules)

    companion object {
        fun createWindowsApp(config: ConfigFacade, os: OsFacade, tiling: TilerFacade, actions: ActionsFacade): AppFacade {
            return App(config, os, tiling, actions)
        }
    }
}
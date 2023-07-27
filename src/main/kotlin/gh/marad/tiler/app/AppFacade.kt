package gh.marad.tiler.app

import gh.marad.tiler.app.internal.App
import gh.marad.tiler.common.filteringrules.FilteringRules
import gh.marad.tiler.os.OsFacade
import gh.marad.tiler.tiler.TilerFacade

interface AppFacade {
    fun start(filteringRules: FilteringRules)

    companion object {
        fun createWindowsApp(os: OsFacade, tiling: TilerFacade): AppFacade {
            return App(os, tiling)
        }
    }
}
package gh.marad.tiler.app.internal

import gh.marad.tiler.app.AppFacade
import gh.marad.tiler.common.filteringrules.FilteringRules
import gh.marad.tiler.os.OsFacade
import gh.marad.tiler.tiler.TilerFacade

class App(val os: OsFacade, val tiling: TilerFacade) : AppFacade {
    override fun start(filteringRules: FilteringRules) {
        os.execute(tiling.initializeWithOpenWindows())
        os.startEventHandling(TilerWindowEventHandler(tiling, filteringRules, os))
    }

}
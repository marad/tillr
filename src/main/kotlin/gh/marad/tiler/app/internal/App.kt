package gh.marad.tiler.app.internal

import gh.marad.tiler.actions.ActionsFacade
import gh.marad.tiler.app.AppFacade
import gh.marad.tiler.common.filteringrules.FilteringRules
import gh.marad.tiler.config.ConfigFacade
import gh.marad.tiler.config.Hotkey
import gh.marad.tiler.os.OsFacade
import gh.marad.tiler.tiler.TilerFacade

class App(val config: ConfigFacade, val os: OsFacade, val tiler: TilerFacade, val actions: ActionsFacade) : AppFacade {
    override fun start(filteringRules: FilteringRules) {
        setupHotkeys(config.getHotkeys())
        actions.registerActionListener(ActionHandler(os, tiler))
        os.execute(tiler.initializeWithOpenWindows())
        os.startEventHandling(TilerWindowEventHandler(tiler, filteringRules, os))
    }

    private fun setupHotkeys(hotkeys: List<Hotkey>) {
        hotkeys.forEach { hotkey ->
            os.registerHotkey(hotkey.key) {
                actions.invokeAction(hotkey.action)
            }
        }
    }
}
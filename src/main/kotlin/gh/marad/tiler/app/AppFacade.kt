package gh.marad.tiler.app

import gh.marad.tiler.actions.ActionsFacade
import gh.marad.tiler.app.internal.App
import gh.marad.tiler.config.ConfigFacade
import gh.marad.tiler.os.OsFacade
import gh.marad.tiler.tiler.TilerFacade

interface AppFacade {
    suspend fun start()
    suspend fun reloadConfig()

    companion object {
        fun createWindowsApp(config: ConfigFacade, os: OsFacade, tiling: TilerFacade, actions: ActionsFacade): AppFacade {
            return App(config, os, tiling, actions)
        }

        fun createAppWithConfig(config: ConfigFacade): AppFacade {
            val os = OsFacade.createWindowsFacade()
            val tiler = TilerFacade.createTiler(config, os)
            val actions = ActionsFacade.createActions()
            return createWindowsApp(config, os, tiler, actions)
        }
    }
}
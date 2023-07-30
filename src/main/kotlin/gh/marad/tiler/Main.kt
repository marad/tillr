package gh.marad.tiler

import gh.marad.tiler.app.AppFacade
import gh.marad.tiler.config.ConfigFacade

// TODO ignore admin windows https://github.com/marad/tillr/issues/1 (https://stackoverflow.com/a/24144277)
// TODO GH Actions CI/CD
// TODO installation script
// TODO handle fullscreen windows
// TODO window showing registered hotkeys
// TODO status toolbar showing current desktop
// TODO [maybe] widgets for status toolbar

fun main() {
    val config = ConfigFacade.createConfig()
    val app = AppFacade.createAppWithConfig(config)
    app.start()
}

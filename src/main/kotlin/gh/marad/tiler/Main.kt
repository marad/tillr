package gh.marad.tiler

import gh.marad.tiler.app.AppFacade
import gh.marad.tiler.config.ConfigFacade
import org.docopt.Docopt
import org.slf4j.LoggerFactory

// TODO ignore admin windows https://github.com/marad/tillr/issues/1 (https://stackoverflow.com/a/24144277)
// TODO GH Actions CI/CD
// TODO installation script
// TODO handle fullscreen windows
// TODO window showing registered hotkeys
// TODO status toolbar showing current desktop
// TODO [maybe] widgets for status toolbar

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("main")
    val data = Docopt("""
       Usage: 
         tillr [--yaml-config=<config-path>]
         tillr -h | --help
         
       Options:
         -h --help     Show this screen.
         -c --config   YAML configuration file path. If not specified default configuration is applied.
    """.trimIndent()).parse(*args)

    val configPath = data["--yaml-config"]?.toString()

    try {
        val config = if (configPath != null) {
            logger.info("Loading configuration at $configPath...")
            ConfigFacade.loadYamlConfig(configPath)
        } else {
            logger.info("Loading default configuration")
            ConfigFacade.createConfig()
        }
        val app = AppFacade.createAppWithConfig(config)
        app.start()
    } catch (ex: Throwable) {
        logger.error("Unexpected error occurred", ex)
    }
}

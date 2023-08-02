package gh.marad.tiler.tools

import gh.marad.tiler.os.OsFacade
import java.util.logging.Logger

fun main() {
    val logger = Logger.getLogger("WindowInspector")
    logger.info("Activate target window!")
    Thread.sleep(1000)
    val os = OsFacade.createWindowsFacade()
    logger.info(os.windowDebugInfo(os.activeWindow()))
}
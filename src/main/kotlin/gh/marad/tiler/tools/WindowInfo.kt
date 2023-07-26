package gh.marad.tiler.tools

import gh.marad.tiler.os.OsFactory

fun main() {
    println("Activate target window!")
    Thread.sleep(1000)
    val os = OsFactory().create()
    println(os.windowDebugInfo(os.activeWindow()))
}
package gh.marad.tiler.os

import gh.marad.tiler.os.internal.WindowsFacade

class OsFactory {
    fun create(): OsFacade {
        return WindowsFacade()
    }
}
package gh.marad.tiler.core.layout

import gh.marad.tiler.core.Windows

interface Layout {
    fun updateSpace(space: LayoutSpace)
    fun retile(windows: Windows): Windows
}
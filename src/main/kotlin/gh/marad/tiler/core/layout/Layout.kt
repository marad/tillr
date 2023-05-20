package gh.marad.tiler.core.layout

import gh.marad.tiler.core.Windows

interface Layout {
    fun retile(windows: Windows, space: LayoutSpace): Windows
}
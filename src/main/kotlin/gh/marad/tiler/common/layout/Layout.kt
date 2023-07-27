package gh.marad.tiler.common.layout

import gh.marad.tiler.common.Windows

interface Layout {
    fun retile(windows: Windows, space: LayoutSpace): Windows
}
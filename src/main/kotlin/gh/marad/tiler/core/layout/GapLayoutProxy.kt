package gh.marad.tiler.core.layout

import gh.marad.tiler.core.Windows
import gh.marad.tiler.core.addGap

class GapLayoutProxy(private val gapSize: Int,
                     private val proxiedLayout: Layout) : Layout {
    override fun retile(windows: Windows, space: LayoutSpace): Windows {
        return addGap(proxiedLayout.retile(windows, space), gapSize)
    }
}
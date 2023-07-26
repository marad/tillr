package gh.marad.tiler.core.layout

import gh.marad.tiler.core.Windows
import gh.marad.tiler.core.addGap

class GapLayoutDecorator(private val gapSize: Int,
                         private val wrappedLayout: Layout) : Layout {
    override fun retile(windows: Windows, space: LayoutSpace): Windows {
        return addGap(wrappedLayout.retile(windows, space), gapSize)
    }
}
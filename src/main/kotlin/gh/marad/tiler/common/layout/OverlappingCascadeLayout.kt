package gh.marad.tiler.common.layout

import gh.marad.tiler.common.WindowPosition
import gh.marad.tiler.common.Windows

class OverlappingCascadeLayout(private val border: Int) : Layout {
    override fun retile(windows: Windows, space: LayoutSpace): Windows {
        if (windows.isEmpty()) return emptyList()

        val widthAndHeightOffset = (windows.size - 1) * border

        val windowPositions = windows.mapIndexed { index, window ->
            val x = space.x + index * border
            val y = space.y + index * border
            val width = space.width - widthAndHeightOffset
            val height = space.height - widthAndHeightOffset
            window.copy(position = WindowPosition(x, y, width, height))
        }
        return windowPositions
    }
}
package gh.marad.tiler.core.layout

import gh.marad.tiler.core.WindowPosition
import gh.marad.tiler.core.Windows

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
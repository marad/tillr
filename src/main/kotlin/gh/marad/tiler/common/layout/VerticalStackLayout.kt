package gh.marad.tiler.common.layout

import gh.marad.tiler.common.Windows

class VerticalStackLayout : Layout {
    override fun retile(windows: Windows, space: LayoutSpace): Windows {
        if (windows.isEmpty()) return emptyList()

        var posY = space.y
        val height = space.height / windows.size
        return windows.map {
            it.reposition(
                x = space.x,
                y = posY,
                width = space.width,
                height = height
            ).also {
                posY += height
            }
        }
    }
}
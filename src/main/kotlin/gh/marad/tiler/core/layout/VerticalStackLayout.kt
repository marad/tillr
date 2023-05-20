package gh.marad.tiler.core.layout

import gh.marad.tiler.core.Windows

class VerticalStackLayout(private var space: LayoutSpace) : Layout {
    override fun retile(windows: Windows): Windows {
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

    override fun updateSpace(space: LayoutSpace) {
        this.space = space
    }
}
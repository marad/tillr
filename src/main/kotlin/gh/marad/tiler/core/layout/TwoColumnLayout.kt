package gh.marad.tiler.core.layout

import gh.marad.tiler.core.Windows

class TwoColumnLayout : Layout {
    private var rightColumnLayout = VerticalStackLayout()

    private fun calcRightColumnSpace(space: LayoutSpace) = LayoutSpace(
        x = space.x + space.width / 2,
        y = space.y,
        width = space.width / 2,
        height = space.height
    )

    override fun retile(windows: Windows, space: LayoutSpace): Windows {
        if (windows.isEmpty()) return emptyList()
        if (windows.size == 1) {
            return listOf(windows.first().reposition(space.x, space.y, space.width, space.height))
        }

        val columnWidth = space.width / 2
        val firstWindow = windows.first().reposition(space.x, space.y, columnWidth, space.height)
        val others = rightColumnLayout.retile(windows.drop(1), calcRightColumnSpace(space))
        return listOf(firstWindow) + others
    }

}
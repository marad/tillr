package gh.marad.tiler.core.layout

import gh.marad.tiler.core.Windows

class TwoColumnLayout(private var ratio: Float = 0.5f) : Layout {
    private var rightColumnLayout = VerticalStackLayout()

    private fun leftColumnWidth(space: LayoutSpace) = (space.width * ratio).toInt()
    private fun rightColumnWidth(space: LayoutSpace) = space.width - leftColumnWidth(space)

    private fun calcRightColumnSpace(space: LayoutSpace) = LayoutSpace(
        x = space.x + leftColumnWidth(space),
        y = space.y,
        width = rightColumnWidth(space),
        height = space.height
    )

    fun increaseRatio(value: Float) {
        ratio = (ratio + value).coerceIn(0.1f, 0.9f)
    }

    fun decreaseRatio(value: Float) {
        ratio = (ratio - value).coerceIn(0.1f, 0.9f)
    }

    override fun retile(windows: Windows, space: LayoutSpace): Windows {
        if (windows.isEmpty()) return emptyList()
        if (windows.size == 1) {
            return listOf(windows.first().reposition(space.x, space.y, space.width, space.height))
        }

        val firstWindow = windows.first().reposition(space.x, space.y, leftColumnWidth(space), space.height)
        val others = rightColumnLayout.retile(windows.drop(1), calcRightColumnSpace(space))
        return listOf(firstWindow) + others
    }

}
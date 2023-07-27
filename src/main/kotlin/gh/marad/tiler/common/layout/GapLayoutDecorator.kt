package gh.marad.tiler.common.layout

import gh.marad.tiler.common.Windows

class GapLayoutDecorator(private val gapSize: Int,
                         private val wrappedLayout: Layout
) : Layout {
    override fun retile(windows: Windows, space: LayoutSpace): Windows {
        return addGap(wrappedLayout.retile(windows, space), gapSize)
    }

    companion object {
        fun addGap(windows: Windows, gap: Int): Windows {
            var minX = Int.MAX_VALUE
            var maxX = Int.MIN_VALUE
            var minY = Int.MAX_VALUE
            var maxY = Int.MIN_VALUE
            windows.forEach {
                minX = minX.coerceAtMost(it.position.x)
                maxX = maxX.coerceAtLeast(it.position.x + it.position.width)
                minY = minY.coerceAtMost(it.position.y)
                maxY = maxY.coerceAtLeast(it.position.y + it.position.height)
            }

            val halfGap = gap / 2

            return windows.map {

                val offsetLeft = if (it.position.x == minX) gap else halfGap
                val offsetRight = offsetLeft + if (it.position.x + it.position.width == maxX) gap else halfGap
                val offsetTop = if (it.position.y == minY) gap else halfGap
                val offsetBottom = offsetTop + if (it.position.y + it.position.height == maxY) gap else halfGap

                it.copy(position = it.position.copy(
                    x = it.position.x + offsetLeft,
                    y = it.position.y + offsetTop,
                    width = it.position.width - offsetRight,
                    height = it.position.height - offsetBottom
                ))
            }
        }
    }
}
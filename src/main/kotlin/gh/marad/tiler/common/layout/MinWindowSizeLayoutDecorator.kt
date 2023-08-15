package gh.marad.tiler.common.layout

import gh.marad.tiler.common.Window
import gh.marad.tiler.common.Windows

class MinWindowSizeLayoutDecorator(val minimumWidth: Int, val minimumHeight: Int, val wrappedLayout: Layout) : Layout {
    override fun retile(windows: Windows, space: LayoutSpace): Windows {
        return wrappedLayout.retile(windows, space).map {
            if (it.isActive) {
                applyMinimumSize(it, space)
            } else {
                it
            }
        }
    }

    private fun applyMinimumSize(window: Window, space: LayoutSpace): Window {
        val widthOffset = (minimumWidth - window.position.width).coerceAtLeast(0)
        val halfWidthOffset = widthOffset / 2
        val heightOffset = (minimumHeight - window.position.height).coerceAtLeast(0)
        val halfHeightOffset = heightOffset / 2

        val resized = with(window.position) {
            copy(
                x = x - halfWidthOffset,
                y = y - halfHeightOffset,
                width = width + widthOffset,
                height = height + heightOffset
            )
        }

        // ensure that edges are not outside the layout space
        val finalPosition = with(resized) {
            val rightOffset = (right - space.right).coerceAtLeast(0)
            val bottomOffset = (bottom - space.bottom).coerceAtLeast(0)
            copy(
                x = x.coerceAtLeast(space.x) - rightOffset,
                y = y.coerceAtLeast(space.y) - bottomOffset,
            )
        }

        return window.copy(position = finalPosition)
    }

    override fun increase(value: Float) {
        wrappedLayout.increase(value)
    }

    override fun decrease(value: Float) {
        wrappedLayout.decrease(value)
    }
}
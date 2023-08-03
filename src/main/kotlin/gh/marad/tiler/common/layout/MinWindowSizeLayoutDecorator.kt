package gh.marad.tiler.common.layout

import gh.marad.tiler.common.Window
import gh.marad.tiler.common.Windows
import gh.marad.tiler.common.navigation.windowDown
import gh.marad.tiler.common.navigation.windowLeft
import gh.marad.tiler.common.navigation.windowRight
import gh.marad.tiler.common.navigation.windowUp

class MinWindowSizeLayoutDecorator(val minimumWidth: Int, val minimumHeight: Int, val wrappedLayout: Layout) : Layout {
    override fun retile(windows: Windows, space: LayoutSpace): Windows {
        return wrappedLayout.retile(windows, space).map {
            if (it.isActive) {
                applyMinimumSize(it, windows)
            } else {
                it
            }
        }
    }

    private fun applyMinimumSize(window: Window, windows: Windows): Window {
        val widthOffset = (minimumWidth - window.position.width).coerceAtLeast(0)
        val heightOffset = (minimumHeight - window.position.height).coerceAtLeast(0)

        val wideWindow = when {
            window.canExpandLeft(windows) -> window.expandWindowLeft(widthOffset)
            window.canExpandRight(windows) -> window.expandWindowRight(widthOffset)
            else -> window
        }

        return when {
            wideWindow.canExpandUp(windows) -> wideWindow.expandWindowUp(heightOffset)
            wideWindow.canExpandDown(windows) -> wideWindow.expandWindowDown(heightOffset)
            else -> window
        }
    }

    private fun Window.expandWindowLeft(offset: Int): Window {
        return copy(position = position.copy(x = position.x - offset, width = position.width + offset))
    }

    private fun Window.expandWindowRight(offset: Int): Window {
        return copy(position = position.copy(width = position.width + offset))
    }

    private fun Window.expandWindowUp(offset: Int): Window {
        return copy(position = position.copy(y = position.y - offset, height = position.height + offset))
    }

    private fun Window.expandWindowDown(offset: Int): Window {
        return copy(position = position.copy(height = position.height + offset))
    }

    private fun Window.canExpandLeft(windows: Windows): Boolean =
        windowLeft(this, windows) != null

    private fun Window.canExpandRight(windows: Windows): Boolean =
        windowRight(this, windows) != null

    private fun Window.canExpandUp(windows: Windows): Boolean =
        windowUp(this, windows) != null

    private fun Window.canExpandDown(windows: Windows): Boolean =
        windowDown(this, windows) != null
}
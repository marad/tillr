package gh.marad.tiler.core.layout

import gh.marad.tiler.core.Window

data class BorderConfig(val width: Int, val windows: (Window) -> Boolean)
data class ComplexLayoutConfig(
    val gap: Int,
    val rightBorder: BorderConfig
)

class ComplexLayout {
}
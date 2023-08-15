package gh.marad.tiler.common.layout

import gh.marad.tiler.common.Window

data class LayoutSpace(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
) {
    val left get() = x
    val right get() = x + width
    val top get() = y
    val bottom get() = y + height

    fun contains(px: Int, py: Int): Boolean {
        return x <= px && px <= x + width && y <= py && py <= y + height
    }

    fun containsWindowCenter(window: Window) = contains(
        px = window.position.centerX(),
        py = window.position.centerY()
    )
}
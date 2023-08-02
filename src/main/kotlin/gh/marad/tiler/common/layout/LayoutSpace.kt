package gh.marad.tiler.common.layout

data class LayoutSpace(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
) {
    fun contains(px: Int, py: Int): Boolean {
        return x <= px && px <= x + width && y <= py && py <= y + height
    }
}
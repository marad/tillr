package gh.marad.tiler.os.internal.winapi

import java.awt.Rectangle

data class MonitorInfo(
    val name: String,
    val displayArea: Rectangle,
    val workArea: Rectangle,
    val isPrimary: Boolean
)
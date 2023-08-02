package gh.marad.tiler.tiler.internal.views

import gh.marad.tiler.common.WindowId
import gh.marad.tiler.common.Windows
import gh.marad.tiler.common.layout.Layout

class View(
    private var windows: MutableList<WindowId> = mutableListOf(),
    var layout: Layout
) {
    private var activeWindow: WindowId? = null

    fun windowToActivate(): WindowId? =
        activeWindow ?: windows.firstOrNull()

    fun addWindow(windowId: WindowId) {
        if (!windows.contains(windowId)) {
            windows.add(windowId)
        }
    }

    fun removeWindow(windowId: WindowId) {
        windows.removeIf { it == windowId }
    }

    fun hasWindow(windowId: WindowId): Boolean = windows.contains(windowId)

    fun filterWindowsInView(windows: Windows): Windows {
        // it's done this way instead of `windows.filter` to preserve
        // the ordering of windows in the view
        val windowsById = windows.associateBy { it.id }
        return this.windows.mapNotNull { windowsById[it] }
    }

    fun filterWindowsNotInView(windows: Windows): Windows = windows.filterNot { hasWindow(it.id) }

    fun updateWindowsInView(windows: List<WindowId>) {
        this.windows.clear()
        this.windows.addAll(windows)
    }

    fun swapWindows(a: WindowId, b: WindowId) {
        val aIdx = windows.indexOf(a)
        val bIdx = windows.indexOf(b)
        if (aIdx < 0 || bIdx < 0) return
        val tmp = windows[aIdx]
        windows[aIdx] = windows[bIdx]
        windows[bIdx] = tmp
    }
}
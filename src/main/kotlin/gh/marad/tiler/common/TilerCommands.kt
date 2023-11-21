package gh.marad.tiler.common

sealed interface TilerCommand

/**
 * Sets window position. It should also un-minimize the window if necessary
 */
data class SetWindowPosition(val windowId: WindowId, val position: WindowPosition) : TilerCommand

/**
 * Minimizes given window
 */
data class HideWindow(val windowId: WindowId) : TilerCommand
data class ShowWindow(val windowId: WindowId) : TilerCommand
data class ActivateWindow(val windowId: WindowId) : TilerCommand
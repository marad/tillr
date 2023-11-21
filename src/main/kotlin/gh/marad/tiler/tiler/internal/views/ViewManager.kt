package gh.marad.tiler.tiler.internal.views

import gh.marad.tiler.common.WindowId
import gh.marad.tiler.common.layout.Layout

class ViewManager(private val defaultLayout: () -> Layout) {
    private var _activeViewId: Int = 0
    private val _views = mutableMapOf(0 to View(layout = defaultLayout()))

    val currentViewId: Int
        get() = _activeViewId

    @Suppress("MemberVisibilityCanBePrivate")
    fun getView(viewId: Int): View = _views.getOrPut(viewId) { View(layout = defaultLayout()) }

    fun currentView(): View {
      return getView(_activeViewId)
    }

    fun changeCurrentView(viewId: Int): View {
        _activeViewId = viewId
        return currentView()
    }

    fun isWindowAssigned(windowId: WindowId): Boolean {
        return _views.values.any {
            it.hasWindow(windowId)
        }
    }

    fun addWindowToView(windowId: WindowId, viewId: Int) {
        getView(viewId).addWindow(windowId)
    }

    fun moveWindow(windowId: WindowId, viewId: Int) {
        currentView().removeWindow(windowId)
        getView(viewId).addWindow(windowId)
    }
}
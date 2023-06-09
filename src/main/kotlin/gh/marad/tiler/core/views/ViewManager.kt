package gh.marad.tiler.core.views

import gh.marad.tiler.core.WindowId
import gh.marad.tiler.core.layout.Layout

class ViewManager(private val defaultLayout: () -> Layout) {
    private var _activeViewId: Int = 0
    private val _views = mutableMapOf(0 to View(layout = defaultLayout()))

    fun getView(viewId: Int): View = _views.getOrPut(viewId) { View(layout = defaultLayout()) }

    fun currentView(): View {
      return getView(_activeViewId)
    }

    fun changeCurrentView(viewId: Int): View {
        _activeViewId = viewId
        return currentView()
    }

    fun moveWindow(windowId: WindowId, viewId: Int) {
        currentView().removeWindow(windowId)
        getView(viewId).addWindow(windowId)
    }
}
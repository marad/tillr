package gh.marad.tiler.app.internal

import gh.marad.tiler.actions.*
import gh.marad.tiler.app.AppFacade
import gh.marad.tiler.common.Window
import gh.marad.tiler.common.Windows
import gh.marad.tiler.common.navigation.windowDown
import gh.marad.tiler.common.navigation.windowLeft
import gh.marad.tiler.common.navigation.windowRight
import gh.marad.tiler.common.navigation.windowUp
import gh.marad.tiler.os.OsFacade
import gh.marad.tiler.tiler.TilerFacade

class ActionHandler(
    private val app: AppFacade,
    private val os: OsFacade,
    private val tiler: TilerFacade,
) : ActionListener {
    override fun onAction(action: Action) {
        when (action) {
            is LayoutDecrease -> {
                tiler.currentViewLayout().decrease(action.amount)
                os.execute(tiler.retile())
            }
            is LayoutIncrease -> {
                tiler.currentViewLayout().increase(action.amount)
                os.execute(tiler.retile())
            }
            is MoveActiveWindowToView -> os.execute(tiler.moveWindow(os.activeWindow(), action.viewId))
            MoveWindowDown -> selectWindow(::windowDown)
            MoveWindowLeft -> selectWindow(::windowLeft)
            MoveWindowRight -> selectWindow(::windowRight)
            MoveWindowUp -> selectWindow(::windowUp)
            SwitchToPreviousView -> os.execute(tiler.switchToPreviousView())
            is SwitchView -> os.execute(tiler.switchToView(action.viewId))
            ReloadConfig -> app.reloadConfig()
        }
    }

    private fun selectWindow(selector: (Window, Windows) -> Window?) {
        val visibleWindows = os.listWindows().filterNot { it.isMinimized }
        val activeWindow = os.activeWindow()
        val window = selector(activeWindow, visibleWindows)
        if (window != null) {
            os.setActiveWindow(window.id)
        }
    }
}
package gh.marad.tiler.app.internal

import gh.marad.tiler.actions.*
import gh.marad.tiler.app.AppFacade
import gh.marad.tiler.common.TilerCommand
import gh.marad.tiler.common.Window
import gh.marad.tiler.common.Windows
import gh.marad.tiler.common.filteringrules.FilteringRules
import gh.marad.tiler.common.navigation.windowDown
import gh.marad.tiler.common.navigation.windowLeft
import gh.marad.tiler.common.navigation.windowRight
import gh.marad.tiler.common.navigation.windowUp
import gh.marad.tiler.os.OsFacade
import gh.marad.tiler.tiler.TilerFacade
import kotlinx.coroutines.channels.Channel
import org.slf4j.LoggerFactory

class ActionHandler(
    private val app: AppFacade,
    private val os: OsFacade,
    private val tiler: TilerFacade,
    private val filteringRules: FilteringRules,
    private val channel: Channel<List<TilerCommand>>
) : ActionListener {
    override suspend fun onAction(action: Action) {
        when (action) {
            is LayoutDecrease -> {
                tiler.currentViewLayout().decrease(action.amount)
                channel.send(tiler.retile())
            }
            is LayoutIncrease -> {
                tiler.currentViewLayout().increase(action.amount)
                channel.send(tiler.retile())
            }
            is MoveActiveWindowToView -> channel.send(tiler.moveWindow(os.activeWindow(), action.viewId))
            MoveWindowDown -> selectWindow(::windowDown)
            MoveWindowLeft -> selectWindow(::windowLeft)
            MoveWindowRight -> selectWindow(::windowRight)
            MoveWindowUp -> selectWindow(::windowUp)
            SwitchToPreviousView -> channel.send(tiler.switchToPreviousView())
            is SwitchView -> channel.send(tiler.switchToView(action.viewId))
            ReloadConfig -> app.reloadConfig()
            ToggleManageWindow -> toggleManageWindow()
        }
    }

    private fun selectWindow(selector: (Window, Windows) -> Window?) {
        val visibleWindows = os.listWindows().filter { it.isVisible }
        val activeWindow = os.activeWindow()
        val window = selector(activeWindow, visibleWindows)
        if (window != null) {
            os.setActiveWindow(window.id)
        }
    }

    private suspend fun toggleManageWindow() {
        val window = os.activeWindow()
        val logger = LoggerFactory.getLogger(ActionHandler::class.java)
        if (filteringRules.shouldManage(window)) {
            filteringRules.ignoreWindow(window)
            tiler.removeWindow(window)
            logger.info("Window ${window.windowName} is ignored now")
        } else {
            filteringRules.manageWindow(window)
            tiler.addWindow(window)
            logger.info("Window ${window.windowName} is managed now")
        }
        channel.send(tiler.retile())
    }
}
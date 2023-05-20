import com.sun.jna.platform.win32.User32
import gh.marad.tiler.winapi.Hotkeys
import gh.marad.tiler.core.*
import gh.marad.tiler.core.filteringrules.FilteringRules
import gh.marad.tiler.core.filteringrules.Rule
import gh.marad.tiler.core.layout.TwoColumnLayout
import gh.marad.tiler.core.views.ViewManager
import gh.marad.tiler.navigation.windowDown
import gh.marad.tiler.navigation.windowLeft
import gh.marad.tiler.navigation.windowRight
import gh.marad.tiler.navigation.windowUp
import gh.marad.tiler.winapi.*
import gh.marad.tiler.windowstiler.*

// TODO better layouts (eg. borders, customizable ratios, BSP layout)
// TODO allow for external configuration
// TODO hotkey to quickly activate/deactivate tiling
// TODO GH Actions CI/CD
// TODO installation script
// TODO handle multiple monitors

fun main() {
    val filteringRules = FilteringRules()

    filteringRules.addAll(listOf(
        Rule.manageIf { it.windowName in listOf("WhatsApp", "Messenger") },
        Rule.manageIf { it.windowName == "Microsoft To Do" && it.className == "ApplicationFrameWindow" },
        Rule.ignoreIf { it.className == "ApplicationFrameTitleBarWindow" },
    ))

    val monitor = Monitors.primary()
    val layout = TwoColumnLayout()
//    val layout = OverlappingCascadeLayout(50)
    val viewManager = ViewManager { layout }
    val windowsTiler = WindowsTiler(viewManager) { getDesktopState(filteringRules) }
    val windowEventHandler = WindowEventHandler(viewManager, windowsTiler, filteringRules, ::windowsUnderCursor)
    val tilerProc = generateEventProcedure(windowEventHandler)

    windowsTiler.initializeWithOpenWindows().execute()
    configureHotkeys(windowsTiler)
    User32.INSTANCE.SetWinEventHook(EVENT_MIN, EVENT_MAX, null, tilerProc, 0, 0, 0)
    windowsMainLoop()
}

private fun configureHotkeys(windowsTiler: WindowsTiler) {
    // desktop switching keys
    val hotkeys = Hotkeys()
    (0..8).forEach { viewId ->
        hotkeys.register("A-${viewId + 1}") {
            windowsTiler.switchToView(viewId).execute()
        }

        hotkeys.register("S-A-${viewId + 1}") {
            windowsTiler.moveWindow(activeWindow().toTilerWindow(), viewId).execute()
            windowsTiler.switchToView(viewId).execute()
        }
    }

    hotkeys.register("A-E") {
        windowsTiler.switchToPreviousView().execute()
    }

    // window navigation keys
    mapOf(
        "L" to ::windowRight,
        "H" to ::windowLeft,
        "J" to ::windowDown,
        "K" to ::windowUp
    ).forEach { (key, selectWindow) ->
        hotkeys.register("A-$key") {
            val visibleWindows = listWindows().filterNot { it.isMinimized() }
            selectWindow(activeWindow(), visibleWindows)
                ?.let { User32.INSTANCE.SetForegroundWindow(it.handle) }
        }
    }
}
import com.sun.jna.platform.win32.User32
import gh.marad.tiler.winapi.Hotkeys
import gh.marad.tiler.core.*
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

val overrides = listOf(
    ManageOverride(true) { win -> win.getTitle() in listOf("WhatsApp", "Messenger") },
    ManageOverride(true) { win -> win.getTitle() == "Microsoft To Do" && win.getRealClassName() == "ApplicationFrameWindow"},
    ManageOverride(false) { win -> win.getRealClassName() == "ApplicationFrameTitleBarWindow" },
    ManageOverride(false) { win -> win.getTitle().isBlank() && win.getProcess().exeName() == "idea64.exe"},
)

fun main(args: Array<String>) {
    val monitor = Monitors.primary()
    val layout = TwoColumnLayout(monitor.workArea.toLayoutSpace())
    val viewManager = ViewManager { layout }
    val windowsTiler = WindowsTiler(viewManager, ::getDesktopState)
    val windowEventHandler = WindowEventHandler(viewManager, windowsTiler, ::windowsUnderCursor)
    val tilerProc = generateEventProcedure(windowEventHandler, viewManager)

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
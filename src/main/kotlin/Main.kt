import com.sun.jna.platform.win32.User32
import gh.marad.tiler.winapi.Hotkeys
import gh.marad.tiler.core.*
import gh.marad.tiler.navigation.windowDown
import gh.marad.tiler.navigation.windowLeft
import gh.marad.tiler.navigation.windowRight
import gh.marad.tiler.navigation.windowUp
import gh.marad.tiler.winapi.*
import gh.marad.tiler.windowstiler.*

// TODO handle multiple monitors
// TODO switch to previous view
// TODO allow for external configuration
// TODO better layouts (eg. borders, customizable ratios, BSP layout)
// TODO hotkey to quickly activate/deactivate tiling
// TODO installation script
// TODO GH Actions CI/CD

val overrides = listOf(
    ManageOverride(true) { win -> win.getTitle() in listOf("WhatsApp", "Messenger") },
    ManageOverride(true) { win -> win.getTitle() == "Microsoft To Do" && win.getRealClassName() == "ApplicationFrameWindow"},
    ManageOverride(false) { win -> win.getRealClassName() == "ApplicationFrameTitleBarWindow" },
    ManageOverride(false) { win -> win.getTitle().isBlank() && win.getProcess().exeName() == "idea64.exe"},
)

fun main(args: Array<String>) {
    val monitor = Monitors.primary()

    val layout = TwoColumnLayout(monitor.workArea.toLayoutSpace())
//    val layout = VerticalStackLayout(monitor.workArea.toLayoutSpace())
    val tiler = Tiler(layout, ::getDesktopState)
    tiler.activateView(0)
    getDesktopState().windows.forEach {
        if (!it.minimized) {
            tiler.windowAppeared(it)
            println("${it.id} - ${it.windowName}")
        }
    }
    tiler.retile().execute()

    // desktop switching keys
    val hotkeys = Hotkeys()
    (0..8).forEach { viewId ->
        hotkeys.register("A-${viewId+1}") {
            tiler.activateView(viewId).execute()
        }

        hotkeys.register("S-A-${viewId+1}") {
            tiler.moveWindow(activeWindow().toTilerWindow(), viewId).debug().execute()
            tiler.activateView(viewId).debug().execute()
        }
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

    val tilerProc = generateEventProcedure(tiler)
    User32.INSTANCE.SetWinEventHook(EVENT_MIN, EVENT_MAX, null, tilerProc, 0, 0, 0)
    windowsMainLoop()
}
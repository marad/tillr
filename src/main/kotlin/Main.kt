import com.sun.jna.platform.win32.User32
import gh.marad.tiler.core.*
import gh.marad.tiler.winapi.*
import gh.marad.tiler.windowstiler.*


// TODO extract code from main to windowstiler module
// TODO handling global key shortcuts (https://github.com/dstjacques/JHotKeys)
// TODO view switching
// TODO allow for external configuration
// TODO better layouts (eg. borders, customizable ratios, BSP layout)

val overrides = listOf(
    ManageOverride(true) { win -> win.getTitle() in listOf("WhatsApp", "Messenger") },
    ManageOverride(true) { win -> win.getTitle() == "Microsoft To Do" && win.getRealClassName() == "ApplicationFrameWindow"},
    ManageOverride(false) { win -> win.getRealClassName() == "ApplicationFrameTitleBarWindow" },
    ManageOverride(false) { win -> win.getTitle().isBlank() && win.getProcess().exeName() == "idea64.exe"},
    ManageOverride(false) { win -> win.getRealClassName() == "TaskManagerWindow" }
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

    val tilerProc = generateEventProcedure(tiler)
    User32.INSTANCE.SetWinEventHook(EVENT_MIN, EVENT_MAX, null, tilerProc, 0, 0, 0)
    windowsMainLoop()
}
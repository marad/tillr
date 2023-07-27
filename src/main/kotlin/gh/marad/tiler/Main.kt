package gh.marad.tiler

import gh.marad.tiler.app.AppFacade
import gh.marad.tiler.common.filteringrules.FilteringRules
import gh.marad.tiler.common.filteringrules.Rule
import gh.marad.tiler.common.layout.GapLayoutDecorator
import gh.marad.tiler.common.layout.TwoColumnLayout
import gh.marad.tiler.common.navigation.windowDown
import gh.marad.tiler.common.navigation.windowLeft
import gh.marad.tiler.common.navigation.windowRight
import gh.marad.tiler.common.navigation.windowUp
import gh.marad.tiler.os.OsFacade
import gh.marad.tiler.tiler.TilerFacade
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.util.logging.Logger

// TODO allow for external configuration
// TODO hotkey to quickly activate/deactivate tiling
// TODO GH Actions CI/CD
// TODO installation script
// TODO handle multiple monitors
// TODO ignore admin windows (https://stackoverflow.com/a/24144277)
// TODO handle fullscreen windows
// TODO window showing registered hotkeys
// TODO status toolbar showing current desktop
// TODO [maybe] widgets for status toolbar


/**
 * Notatki o wykorzystaniu KScript
 * Aktualne API wydaje się być trochę bałaganiarskie. Tworzenie poszczególnych elementów
 * jest zagmatwane i nieliniowe.
 *
 * Chciałbym aby użytkownik API mógł w prosty sposób skonfigurować to co dla niego istotne:
 * - jakie okna mają być ignorowane/zarządzane (reguły filtrowania)
 * - skróty klawiszowe
 * - używany layout - idealnie jeśli użytkownik mógłby użyć jakiś swój
 * - potencjalnie mógłby też chcieć zapiąć się na eventy okien lub z tilera
 *
 * Wydaje się, że żeby to umożliwić trzeba utworzyć jakieś jedno spójne API,
 * które będzie interfejsem dla użytkownika do konfiguracji tych wszystkich rzeczy.
 */

fun main() {
    val filteringRules = FilteringRules()

    filteringRules.addAll(listOf(
        Rule.manageIf { it.windowName in listOf("WhatsApp", "Messenger") },
        Rule.manageIf { it.windowName == "Microsoft To Do" && it.className == "ApplicationFrameWindow" },
        Rule.ignoreIf { it.className == "ApplicationFrameTitleBarWindow" },
    ))

    val twoColumnLayout = TwoColumnLayout(0.55f)
    val layout = GapLayoutDecorator(20, twoColumnLayout)

    val os = OsFacade.create()
    val tiler = TilerFacade.windowsTiler(layout, filteringRules, os)
    val app = AppFacade.createWindowsApp(os, tiler)

//    os.execute(windowsTiler.initializeWithOpenWindows())

    @Suppress("UNUSED_VARIABLE") val trayIcon: TrayIcon = createTrayIcon(os, tiler)
    configureHotkeys(tiler, twoColumnLayout, os)

    // TODO event handler można zastąpić jakimś event bus'em
//    val windowEventHandler = TilerWindowEventHandler(windowsTiler, filteringRules, os)
//    os.startEventHandling(windowEventHandler)
    app.start(filteringRules)
}

fun createTrayIcon(os: OsFacade, windowsTiler: TilerFacade): TrayIcon {
    val icon = Toolkit.getDefaultToolkit().getImage(AppFacade::class.java.getResource("/icon.png"))
    val stopped = Toolkit.getDefaultToolkit().getImage(AppFacade::class.java.getResource("/stopped_icon.png"))
    val trayIcon = TrayIcon(icon, "Tiler")
    trayIcon.isImageAutoSize = true
    trayIcon.addActionListener {
        Logger.getLogger("createTrayIcon").info("Event: ${it.actionCommand}")
    }

    trayIcon.addMouseListener(object : java.awt.event.MouseAdapter() {
        override fun mouseClicked(e: java.awt.event.MouseEvent?) {
            if (e?.button == java.awt.event.MouseEvent.BUTTON1) {
                if (trayIcon.image == stopped) {
                    trayIcon.image = icon
                    windowsTiler.enabled = true
                    os.execute(windowsTiler.retile())
                } else {
                    trayIcon.image = stopped
                    windowsTiler.enabled = false
                }
            }
        }
    })

    val popupMenu = java.awt.PopupMenu()
    popupMenu.add(java.awt.MenuItem("Exit")).addActionListener {
        System.exit(0)
    }
    trayIcon.popupMenu = popupMenu

    SystemTray.getSystemTray().add(trayIcon)
    return trayIcon
}

private fun configureHotkeys(windowsTiler: TilerFacade, layout: TwoColumnLayout, os: OsFacade) {
    // desktop switching keys
    (0..8).forEach { viewId ->
        os.registerHotkey("A-${viewId + 1}") {
            os.execute(windowsTiler.switchToView(viewId))
        }

        os.registerHotkey("S-A-${viewId + 1}") {
            os.execute(windowsTiler.moveWindow(os.activeWindow(), viewId))
            os.execute(windowsTiler.switchToView(viewId))
        }
    }

    os.registerHotkey("A-E") {
        os.execute(windowsTiler.switchToPreviousView())
    }

    // window navigation keys
    mapOf(
        "L" to ::windowRight,
        "H" to ::windowLeft,
        "J" to ::windowDown,
        "K" to ::windowUp
    ).forEach { (key, selectWindow) ->
        os.registerHotkey("A-$key") {
            val visibleWindows = os.listWindows().filterNot { it.isMinimized }
            selectWindow(os.activeWindow(), visibleWindows)
                ?.let { os.setActiveWindow(it.id) }
        }
    }

    os.registerHotkey("S-A-L") {
        layout.increaseRatio(0.05f)
        os.execute(windowsTiler.retile())
    }

    os.registerHotkey("S-A-H") {
        layout.decreaseRatio(0.05f)
        os.execute(windowsTiler.retile())
    }
}
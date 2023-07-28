package gh.marad.tiler

import gh.marad.tiler.actions.ActionsFacade
import gh.marad.tiler.app.AppFacade
import gh.marad.tiler.common.filteringrules.FilteringRules
import gh.marad.tiler.common.filteringrules.Rule
import gh.marad.tiler.config.ConfigFacade
import gh.marad.tiler.os.OsFacade
import gh.marad.tiler.tiler.TilerFacade
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.util.logging.Logger

// TODO ignore admin windows https://github.com/marad/tillr/issues/1 (https://stackoverflow.com/a/24144277)
// TODO allow for external configuration
// TODO GH Actions CI/CD
// TODO installation script
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

    filteringRules.addAll(
        listOf(
            Rule.manageIf { it.windowName in listOf("WhatsApp", "Messenger") },
            Rule.manageIf { it.windowName == "Microsoft To Do" && it.className == "ApplicationFrameWindow" },
            Rule.ignoreIf { it.className == "ApplicationFrameTitleBarWindow" },
        )
    )


    val config = ConfigFacade.createConfig()
    val os = OsFacade.createWindowsFacade()
    val tiler = TilerFacade.createTiler(config, filteringRules, os)
    val actions = ActionsFacade.createActions()
    val app = AppFacade.createWindowsApp(config, os, tiler, actions)


    @Suppress("UNUSED_VARIABLE") val trayIcon: TrayIcon = createTrayIcon(os, tiler)

    app.start(filteringRules)
}

fun createTrayIcon(os: OsFacade, tiler: TilerFacade): TrayIcon {
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
                    tiler.enabled = true
                    os.execute(tiler.retile())
                } else {
                    trayIcon.image = stopped
                    tiler.enabled = false
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
package gh.marad.tiler.app.internal

import gh.marad.tiler.actions.ActionsFacade
import gh.marad.tiler.app.AppFacade
import gh.marad.tiler.common.filteringrules.FilteringRules
import gh.marad.tiler.config.ConfigFacade
import gh.marad.tiler.config.Hotkey
import gh.marad.tiler.os.OsFacade
import gh.marad.tiler.tiler.TilerFacade
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.util.logging.Logger

class App(val config: ConfigFacade, val os: OsFacade, val tiler: TilerFacade, val actions: ActionsFacade) : AppFacade {
    @Suppress("UNUSED_VARIABLE")
    override fun start(filteringRules: FilteringRules) {
        val trayIcon = createTrayIcon(os, tiler)
        setupHotkeys(config.getHotkeys())
        actions.registerActionListener(ActionHandler(os, tiler))
        os.execute(tiler.initializeWithOpenWindows())
        os.startEventHandling(TilerWindowEventHandler(tiler, filteringRules, os))
    }

    private fun setupHotkeys(hotkeys: List<Hotkey>) {
        hotkeys.forEach { hotkey ->
            os.registerHotkey(hotkey.key) {
                actions.invokeAction(hotkey.action)
            }
        }
    }

    private fun createTrayIcon(os: OsFacade, tiler: TilerFacade): TrayIcon {
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
}
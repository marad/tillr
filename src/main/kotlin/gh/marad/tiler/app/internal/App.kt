package gh.marad.tiler.app.internal

import gh.marad.tiler.actions.ActionsFacade
import gh.marad.tiler.actions.ReloadConfig
import gh.marad.tiler.app.AppFacade
import gh.marad.tiler.common.BroadcastingEventHandler
import gh.marad.tiler.config.ConfigFacade
import gh.marad.tiler.config.Hotkey
import gh.marad.tiler.os.OsFacade
import gh.marad.tiler.tiler.TilerFacade
import org.slf4j.LoggerFactory
import java.awt.MenuItem
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon

class App(val config: ConfigFacade, val os: OsFacade, val tiler: TilerFacade, val actions: ActionsFacade) : AppFacade {
    private val logger = LoggerFactory.getLogger(App::class.java)

    @Suppress("UNUSED_VARIABLE")
    override fun start() {
        val trayIcon = createTrayIcon(os, tiler, actions)
        setupHotkeys(config.getHotkeys())
        actions.registerActionListener(ActionHandler(this, os, tiler))
        val executor = TilerCommandsExecutorAndWatcher(os, config.getFilteringRules())
        executor.execute(tiler.initializeWithOpenWindows())
        val evenHandler = BroadcastingEventHandler(
            TilerWindowEventHandler(tiler, config.getFilteringRules(), os, executor),
            RestoreWindowsOnExitEventHandler(os)
        )
        os.startEventHandling(evenHandler)
    }

    override fun reloadConfig() {
        logger.info("Reloading config...")
        config.reload()
        logger.info("Updating hotkeys...")
        os.clearHotkeys()
        setupHotkeys(config.getHotkeys())
        logger.info("Hotkeys updated...")
        os.execute(tiler.retile())
        logger.info("Config reloaded!")
    }

    private fun setupHotkeys(hotkeys: List<Hotkey>) {
        hotkeys.forEach { hotkey ->
            os.registerHotkey(hotkey.key) {
                actions.invokeAction(hotkey.action)
            }
        }
    }

    private fun createTrayIcon(os: OsFacade, tiler: TilerFacade, actions: ActionsFacade): TrayIcon {
        val icon = Toolkit.getDefaultToolkit().getImage(AppFacade::class.java.getResource("/icon.png"))
        val stopped = Toolkit.getDefaultToolkit().getImage(AppFacade::class.java.getResource("/stopped_icon.png"))
        val trayIcon = TrayIcon(icon, "Tiler")
        trayIcon.isImageAutoSize = true
        trayIcon.addActionListener {
            logger.info("Event: ${it.actionCommand}")
        }

        trayIcon.addMouseListener(object : java.awt.event.MouseAdapter() {
            override fun mouseClicked(e: java.awt.event.MouseEvent?) {
                // TODO: This should be extracted to actions
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
        if (config.getConfigPath() != null) {
            popupMenu.add(MenuItem("Edit config")).addActionListener {
                val editor = ProcessBuilder(config.configEditorPath(), config.getConfigPath())
                editor.start()
            }
            popupMenu.add(MenuItem("Reload config")).addActionListener {
                actions.invokeAction(ReloadConfig)
            }
            popupMenu.addSeparator()
        }
        popupMenu.add(MenuItem("Exit")).addActionListener {
            System.exit(0)
        }
        trayIcon.popupMenu = popupMenu

        SystemTray.getSystemTray().add(trayIcon)
        return trayIcon
    }
}
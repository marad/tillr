package gh.marad.tiler.help

import gh.marad.tiler.common.Window
import gh.marad.tiler.os.OsFacade
import gh.marad.tiler.os.WindowEventHandler
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import javax.swing.*

class WindowInspector(private val osFacade: OsFacade) : JFrame("Tiler Window Inspector"), WindowEventHandler {
    private val btn = JButton("Inspect next window")
    private val pane = JEditorPane()
    private var capturing = false
    init {
        val uiFont = Font.getFont("Segoe UI")
        font = uiFont
        btn.font = uiFont
        pane.font = uiFont
        pane.isEditable = false
        isAlwaysOnTop = true
        defaultCloseOperation = WindowConstants.HIDE_ON_CLOSE
        preferredSize = Dimension(600, 400)
        contentPane.add(btn, BorderLayout.NORTH)
        contentPane.add(pane, BorderLayout.CENTER)
        pack()

        btn.addActionListener {
            pane.text = "Activate a window to inspect..."
            capturing = true
        }
        pane.text = osFacade.windowDebugInfo(osFacade.activeWindow())
    }

    override suspend fun windowActivated(window: Window) {
        if (capturing) {
            pane.text = osFacade.windowDebugInfo(window)
            capturing = false
        }
    }

    override suspend fun windowAppeared(window: Window) { }
    override suspend fun windowDisappeared(window: Window) { }
    override suspend fun windowMinimized(window: Window) { }
    override suspend fun windowRestored(window: Window) { }
    override suspend fun windowMovedOrResized(window: Window) { }
}

fun main() {
    val osFacade = OsFacade.createWindowsFacade()
    val win = WindowInspector(osFacade)
    win.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
    win.isVisible = true

    OsFacade.createWindowsFacade().startEventHandling(win)
}
package gh.marad.tiler

import com.sun.jna.platform.win32.User32
import gh.marad.tiler.winapi.DwmApi
import gh.marad.tiler.winapi.Window
import gh.marad.tiler.winapi.windowBorders

fun main() {
    println("Activate target window!")
    Thread.sleep(1000)
    val hwnd = User32.INSTANCE.GetForegroundWindow()
    val window = Window(hwnd)
    println("Parent: ${User32.INSTANCE.GetParent(hwnd)}")
    println("Name: ${window.getTitle()}")
    println("Class: ${window.getRealClassName()}")
    println("Process: ${window.getProcess().exePath()}")
    println("Pos: ${window.getPos()}")
    println("Minimized: ${window.isMinimized()}")
    println("Maximized: ${window.isMaximized()}")
    println("Visible: ${window.isVisible()}")
    println("Popup: ${window.isPopup()}")
    println("Visible frame border thickness: ${DwmApi.getVisibleFrameBorderThickness(hwnd)}")
    println("Window borders: ${windowBorders(hwnd)}")
    println("Style / caption: ${window.getStyle().caption()}")
    println("Style / border: ${window.getStyle().border()}")
    println("Style / sysMenu: ${window.getStyle().sysMenu()}")
    println("ExStyle / clientEdge: ${window.getExStyle().clientEdge()}")
    println("ExStyle / appWindow: ${window.getExStyle().appWindow()}")
    println("ExStyle / windowEdge: ${window.getExStyle().windowEdge()}")
    println("SM_CYCAPTION: ${User32.INSTANCE.GetSystemMetrics(User32.SM_CYCAPTION)}")
}
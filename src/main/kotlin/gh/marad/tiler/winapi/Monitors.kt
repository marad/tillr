package gh.marad.tiler.winapi

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser

private val u32 = User32.INSTANCE

object Monitors {
    fun primary(): MonitorInfo = list().find { it.isPrimary }!!
    fun count() = User32.INSTANCE.GetSystemMetrics(User32.SM_CMONITORS)
    fun list(): List<MonitorInfo> {
        val monitors = mutableListOf<MonitorInfo>()
        val listMonitors =
            WinUser.MONITORENUMPROC { monitor: WinUser.HMONITOR, hdc: WinDef.HDC?, rect: WinDef.RECT, param: WinDef.LPARAM ->
                val info = WinUser.MONITORINFOEX()
                u32.GetMonitorInfo(monitor, info)
                monitors.add(
                    MonitorInfo(
                        name = info.szDevice.concatToString().trim(Char(0)),
                        displayArea = info.rcMonitor.toRectangle(),
                        workArea = info.rcWork.toRectangle(),
                        isPrimary = (info.dwFlags and WinUser.MONITORINFOF_PRIMARY) == WinUser.MONITORINFOF_PRIMARY
                    )
                )
                0
            }
        u32.EnumDisplayMonitors(null, null, listMonitors , WinDef.LPARAM(0))
        return monitors
    }
}
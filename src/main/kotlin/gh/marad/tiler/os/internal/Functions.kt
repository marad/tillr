package gh.marad.tiler.os.internal

import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser
import gh.marad.tiler.os.WindowEventHandler
import gh.marad.tiler.os.internal.winapi.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

val ignoredEvents = arrayOf(
    EVENT_OBJECT_LOCATIONCHANGE, EVENT_OBJECT_NAMECHANGE, EVENT_SYSTEM_CAPTURESTART,
    EVENT_SYSTEM_CAPTUREEND, EVENT_OBJECT_DESTROY, EVENT_OBJECT_CREATE,
    EVENT_OBJECT_PARENTCHANGE, EVENT_OBJECT_REORDER
)

fun generateEventProcedure(eventHandler: WindowEventHandler): WinUser.WinEventProc {
    return WinUser.WinEventProc { hWinEventHook, event, hwnd, idObject, idChild, dwEventThread, dwmsEventTime ->
        if (hwnd == null || event in ignoredEvents) return@WinEventProc

        val window = Window(hwnd)

        if(shouldIgnoreEvent(window, idObject)) {
            return@WinEventProc
        }

        try {
            runBlocking(Dispatchers.IO) {
                when (event) {
                    EVENT_SYSTEM_FOREGROUND -> eventHandler.windowActivated(window.toTilerWindow())
                    EVENT_OBJECT_FOCUS -> {}
                    EVENT_OBJECT_SHOW -> eventHandler.windowAppeared(window.toTilerWindow())
                    EVENT_OBJECT_DESTROY -> eventHandler.windowDisappeared(window.toTilerWindow())
                    EVENT_OBJECT_HIDE -> eventHandler.windowDisappeared(window.toTilerWindow())
                    EVENT_SYSTEM_MINIMIZESTART -> eventHandler.windowMinimized(window.toTilerWindow())
                    EVENT_SYSTEM_MINIMIZEEND -> eventHandler.windowRestored(window.toTilerWindow())
                    EVENT_SYSTEM_MOVESIZESTART -> {}
                    EVENT_SYSTEM_MOVESIZEEND -> {
                        eventHandler.windowMovedOrResized(window.toTilerWindow())
                    }

                    else -> {}
                }
            }
        }
        catch (_: CannotGetWindowPositionException) {
            // ignore this exception
        }
        catch (e: Exception) {
            val logger = LoggerFactory.getLogger("generateEventProcedure")
            logger.error("Error while handling event $event for window $window", e)
        }
    }
}

fun shouldIgnoreEvent(
    window: Window,
    idObject: WinDef.LONG?
): Boolean {
    val isNotForWindow = idObject != OBJID_WINDOW
    return isNotForWindow || !isInterestingWindow(window)
}

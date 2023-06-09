package gh.marad.tiler.winapi

import com.melloware.jintellitype.HotkeyListener
import com.melloware.jintellitype.JIntellitype
import kotlin.system.exitProcess

typealias Handler = () -> Unit

data class Shortcut(val modifiers: Int, val key: Int) {
    companion object {
        fun parse(shortcut: String): Shortcut? {
            if (shortcut.isBlank()) return null

            val parts = shortcut.split("-")
            val code = parts.last()[0].code
            val modifiers = parts.dropLast(1)
                .fold(0) { acc, modChar ->
                    val modCode = when(modChar[0]) {
                        'S' -> JIntellitype.MOD_SHIFT
                        'C' -> JIntellitype.MOD_CONTROL
                        'M' -> JIntellitype.MOD_WIN
                        'A' -> JIntellitype.MOD_ALT
                        else -> 0
                    }
                    acc or modCode
                }
            return Shortcut(modifiers, code)
        }
    }
}

class Hotkeys : HotkeyListener {
    private val handlers = arrayListOf<Handler>()

    init {
        if (!JIntellitype.isJIntellitypeSupported()) {
            System.err.println("Intellitype is not supported - can't register any hotkeys!")
            exitProcess(1)
        }
        val it = JIntellitype.getInstance()
        it.registerHotKey(1, JIntellitype.MOD_ALT, '1'.code)

        it.addHotKeyListener(this)
    }

    fun register(shortcutText: String, handler: Handler): Boolean {
        val id = handlers.size + 1
        val shortcut = Shortcut.parse(shortcutText)
        if (shortcut == null) {
            return false
        } else {
            handlers.add(handler)
            JIntellitype.getInstance().registerHotKey(id, shortcut.modifiers, shortcut.key)
            return true
        }
    }

    override fun onHotKey(identifier: Int) {
        handlers[identifier-1]()
    }
}
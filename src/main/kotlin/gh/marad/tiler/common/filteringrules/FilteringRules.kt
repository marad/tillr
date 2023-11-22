package gh.marad.tiler.common.filteringrules

import gh.marad.tiler.common.Window
import java.util.Collections

@Suppress("unused")
class FilteringRules {
    private val rules = Collections.synchronizedList(mutableListOf(*CoreRules))

    fun add(rule: Rule) {
        rules.add(rule)
    }

    fun manageWindow(window: Window) {
        removeRulesForWindow(window)
        add(Rule.manageIf { it.id == window.id })
    }

    fun ignoreWindow(window: Window) {
        removeRulesForWindow(window)
        add(Rule.ignoreIf { it.id == window.id })
    }

    fun addAll(rules: List<Rule>) {
        this.rules.addAll(rules)
    }

    fun clear(applyCoreRules: Boolean = true) {
        rules.clear()
        if (applyCoreRules) {
            rules.addAll(CoreRules)
        }
    }

    fun shouldManage(window: Window): Boolean = rules.lastOrNull {
        it.matches(window)
    }?.shouldManage ?: true

    private fun removeRulesForWindow(window: Window) {
        val it = rules.iterator()
        for (rule in it) {
            if (rule.matches(window)) {
                it.remove()
            }
        }
    }

    companion object {
        val CoreRules = arrayOf(
            Rule.ignoreIf { it.isPopup },
            Rule.ignoreIf { it.exeName == "idea64.exe" && it.windowName.isBlank() },
            Rule.ignoreIf { it.exeName == "PowerToys.MeasureToolUI.exe" || it.exeName == "PowerToys.Settings.exe" },
            Rule.ignoreIf { it.windowName == "PopupMessageWindow" }
        )
    }
}
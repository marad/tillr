package gh.marad.tiler.common.filteringrules

import gh.marad.tiler.common.Window

@Suppress("unused")
class FilteringRules {
    private val rules = mutableListOf(*CoreRules)

    fun add(rule: Rule) {
        rules.add(rule)
    }

    fun addAll(rules: List<Rule>) {
        this.rules.addAll(rules)
    }

    fun clear(applyCoreRules: Boolean = true) {
        rules.clear()
        if(applyCoreRules) {
            rules.addAll(CoreRules)
        }
    }

    fun shouldManage(window: Window): Boolean = rules.lastOrNull { it.matches(window) }?.shouldManage ?: true

    companion object {
        val CoreRules = arrayOf(
            Rule.ignoreIf { it.isPopup },
            Rule.ignoreIf { it.exeName == "idea64.exe" && it.windowName.isBlank() },
            Rule.ignoreIf { it.exeName == "PowerToys.MeasureToolUI.exe" || it.exeName == "PowerToys.Settings.exe" },
        )
    }
}
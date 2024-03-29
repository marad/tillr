package gh.marad.tiler.config.internal

import gh.marad.tiler.actions.*
import gh.marad.tiler.common.Window
import gh.marad.tiler.common.assignments.WindowAssignments
import gh.marad.tiler.common.filteringrules.FilteringRules
import gh.marad.tiler.common.filteringrules.Rule
import gh.marad.tiler.common.layout.GapLayoutDecorator
import gh.marad.tiler.common.layout.Layout
import gh.marad.tiler.common.layout.MinWindowSizeLayoutDecorator
import gh.marad.tiler.common.layout.TwoColumnLayout
import gh.marad.tiler.config.ConfigException
import gh.marad.tiler.config.ConfigFacade
import gh.marad.tiler.config.Hotkey
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class YamlConfig(configPath: String) : ConfigFacade {
    val configPath = Paths.get(configPath)
    private val logger = LoggerFactory.getLogger(YamlConfig::class.java)
    private var layoutCreator: () -> Layout = { TwoColumnLayout(0.55f) }
    private val filteringRules: FilteringRules = FilteringRules()
    private val hotkeys = mutableListOf<Hotkey>()
    private var configEditorPath: String = "notepad.exe"

    init {
        loadConfig()
    }

    fun loadConfig() {
        logger.info("Loading config from $configPath...")
        if (Files.notExists(configPath)) {
            throw ConfigFileMissing(configPath)
        }
        val fileStream = Files.newInputStream(configPath, StandardOpenOption.READ)
        val yaml = Yaml()
        val data = yaml.load<Map<String, Any>>(fileStream)
        if (data != null) {
            configEditorPath = data["editor"]?.toString() ?: configEditorPath
            readLayout(data["layout"])
            readFilteringRules(data["rules"])
            readHotkeys(data["hotkeys"])
        }
    }

    override fun reload() {
        loadConfig()
    }

    override fun createLayout(): Layout {
        return layoutCreator()
    }

    override fun getHotkeys(): List<Hotkey> {
        return hotkeys
    }

    override fun getFilteringRules(): FilteringRules {
        return filteringRules
    }

    override fun getAssignments(): WindowAssignments {
        return WindowAssignments()
    }

    override fun getConfigPath(): String {
        return configPath.toAbsolutePath().toString()
    }

    override fun configEditorPath(): String {
        return configEditorPath
    }

    @Suppress("UNCHECKED_CAST")
    private fun readLayout(section: Any?) {
        val data = if (section != null) {
            section as Map<String, Any>
        } else {
            return
        }

        val name = data["name"]?.toString() ?: "TwoColumnLayout"
        val gap = data["gap"]?.toString()?.toInt() ?: 0
        val ratio = data["ratio"]?.toString()?.toFloat() ?: 0.55f
        val minSize = data["minSize"] as Map<String, Any>?

        layoutCreator = when (name) {
            "TwoColumnLayout" -> { {TwoColumnLayout(ratio)} }
            else -> throw IllegalArgumentException("Unknown layout name: $name")
        }

        if (minSize != null) {
            val wrappedLayout = layoutCreator
            layoutCreator = {
                MinWindowSizeLayoutDecorator(
                    minimumWidth = minSize["width"]?.toString()?.toInt() ?: 1,
                    minimumHeight = minSize["height"]?.toString()?.toInt() ?: 1,
                    wrappedLayout = wrappedLayout()
                )
            }
        }

        if (gap != 0) {
            val wrappedLayout = layoutCreator
            layoutCreator = { GapLayoutDecorator(gap, wrappedLayout()) }
        }
    }

    private fun readFilteringRules(section: Any?) {
        val rules = if (section != null) {
            @Suppress("UNCHECKED_CAST")
            section as List<Map<String, Any>>
        } else {
            return
        }

        filteringRules.clear()
        rules.forEach { ruleSpec ->
            val title = ruleSpec["title"]?.toString()
            val className = ruleSpec["class"]?.toString()
            val exeName = ruleSpec["exeName"]?.toString()
            val should = ruleSpec["should"].toString()

            val check = { it: Window ->
                (title == null || it.windowName == title) &&
                (className == null || it.className == className) &&
                (exeName == null || it.exeName == exeName)
            }

            val rule = when (should) {
                "manage" -> Rule.manageIf(check)
                "ignore" -> Rule.ignoreIf(check)
                else -> throw IllegalArgumentException("Unknown rule type: $title")
            }
            filteringRules.add(rule)
        }

    }

    private fun readHotkeys(section: Any?) {
        val hotkeysConfig = if (section != null) {
            @Suppress("UNCHECKED_CAST")
            section as List<Map<String, Any>>
        } else {
            return
        }
        hotkeys.clear()
        hotkeysConfig.forEach { map ->
            val key = map["key"].toString()
            val actionName = map["action"].toString()
            val value = map["value"].toString()
            val action = when(actionName) {
                "SwitchView" -> SwitchView(value.toInt())
                "MoveActiveWindowToView" -> MoveActiveWindowToView(value.toInt())
                "SwitchToPreviousView" -> SwitchToPreviousView
                "MoveWindowRight" -> MoveWindowRight
                "MoveWindowLeft" -> MoveWindowLeft
                "MoveWindowUp" -> MoveWindowUp
                "MoveWindowDown" -> MoveWindowDown
                "LayoutIncrease" -> LayoutIncrease(value.toFloat())
                "LayoutDecrease" -> LayoutDecrease(value.toFloat())
                "ReloadConfig" -> ReloadConfig
                "ToggleManageWindow" -> ToggleManageWindow
                else -> throw IllegalArgumentException("Unknown action: $actionName")
            }
            hotkeys.add(Hotkey(key, action))
        }
    }

    data class ConfigFileMissing(val path: Path)
        : ConfigException("Configuration file '$path' does not exist")
}
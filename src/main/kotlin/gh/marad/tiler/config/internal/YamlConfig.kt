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

    init {
        loadConfig()
    }

    @Suppress("UNCHECKED_CAST")
    fun loadConfig() {
        logger.info("Loading config from $configPath...")
        if (Files.notExists(configPath)) {
            throw ConfigFileMissing(configPath)
        }
        val fileStream = Files.newInputStream(configPath, StandardOpenOption.READ)
        val yaml = Yaml()
        val data = yaml.load<Map<String, Any>>(fileStream)
        readLayout(data["layout"] as Map<String, Any>)
        readFilteringRules(data["rules"] as List<Map<String, Any>>)
        readHotkeys(data["hotkeys"] as List<Map<String, Any>>)
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
        TODO("Not yet implemented")
    }

    @Suppress("UNCHECKED_CAST")
    private fun readLayout(data: Map<String, Any>) {
        val name = data["name"].toString()
        val gap = data["gap"].toString().toInt()
        val ratio =  data["ratio"].toString().toFloat()
        val minSize = data["minSize"] as Map<String, Any>

        layoutCreator = when (name) {
            "TwoColumnLayout" -> { {TwoColumnLayout(ratio)} }
            else -> throw IllegalArgumentException("Unknown layout name: $name")
        }

        if (minSize != null) {
            val wrappedLayout = layoutCreator
            layoutCreator = { MinWindowSizeLayoutDecorator(
                minimumWidth = minSize["width"]?.toString()?.toInt() ?: 1,
                minimumHeight = minSize["height"]?.toString()?.toInt()  ?: 1,
                wrappedLayout = wrappedLayout()
            ) }
        }

        if (gap != 0) {
            val wrappedLayout = layoutCreator
            layoutCreator = { GapLayoutDecorator(gap, wrappedLayout()) }
        }
    }

    private fun readFilteringRules(rules: List<Map<String, Any>>) {
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
            filteringRules.clear()
            filteringRules.add(rule)
        }

    }

    private fun readHotkeys(hotkeysConfig: List<Map<String, Any>>) {
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
                else -> throw IllegalArgumentException("Unknown action: $actionName")
            }
            hotkeys.add(Hotkey(key, action))
        }
    }

    data class ConfigFileMissing(val path: Path)
        : ConfigException("Configuration file '$path' does not exist")
}
package gh.marad.tiler.config

import gh.marad.tiler.common.assignments.WindowAssignments
import gh.marad.tiler.common.filteringrules.FilteringRules
import gh.marad.tiler.common.layout.Layout
import gh.marad.tiler.config.internal.YamlConfig
import gh.marad.tiler.os.OsFacade
import java.nio.file.Paths
import kotlin.io.path.exists

interface ConfigFacade {
    fun reload()
    fun createLayout(): Layout
    fun getHotkeys(): List<Hotkey>
    fun getFilteringRules(): FilteringRules
    fun getAssignments(): WindowAssignments
    fun getConfigPath(): String?
    fun configEditorPath(): String

    companion object {
        fun createConfig(osFacade: OsFacade): ConfigFacade {
            val defaultConfigPath = Paths.get(osFacade.userHome(), ".config", "tiler", "config.yaml")
            if (!defaultConfigPath.exists()) {
                defaultConfigPath.parent.toFile().mkdirs()
                defaultConfigPath.toFile().createNewFile()
            }
            return loadYamlConfig(defaultConfigPath.toString())
        }

        fun loadYamlConfig(path: String): ConfigFacade =
            YamlConfig(path)
    }
}
package gh.marad.tiler.app.internal

import gh.marad.tiler.common.SetWindowPosition
import gh.marad.tiler.common.TilerCommand
import gh.marad.tiler.common.filteringrules.FilteringRules
import gh.marad.tiler.common.filteringrules.Rule
import gh.marad.tiler.os.OsFacade

class TilerCommandsExecutorAndWatcher(
    private val os: OsFacade,
    private val filteringRules: FilteringRules,
) {
    fun execute(commands: List<TilerCommand>) {
        os.execute(commands)
        excludeWindowsThatDidNotMove(commands)
    }

    private fun excludeWindowsThatDidNotMove(commands: List<TilerCommand>) {
        commands.forEach { command ->
            if (command is SetWindowPosition && !os.isWindowAtPosition(command.windowId, command.position)) {
                // remove misbehaving window from tiling
                filteringRules.add(Rule.ignoreIf { it.id == command.windowId })
            }
        }
    }
}
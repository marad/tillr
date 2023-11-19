package gh.marad.tiler.config.internal

import gh.marad.tiler.actions.*
import gh.marad.tiler.common.assignments.WindowAssignments
import gh.marad.tiler.common.filteringrules.FilteringRules
import gh.marad.tiler.common.filteringrules.Rule
import gh.marad.tiler.common.layout.Layout
import gh.marad.tiler.common.layout.TwoColumnLayout
import gh.marad.tiler.config.ConfigFacade
import gh.marad.tiler.config.Hotkey

class SimpleConfig : ConfigFacade {
    private val filteringRules = FilteringRules().also { rules ->
        rules.addAll(
            listOf(
                Rule.manageIf { it.windowName in listOf("WhatsApp", "Messenger") },
                Rule.manageIf { it.windowName == "Microsoft To Do" && it.className == "ApplicationFrameWindow" },
                Rule.ignoreIf { it.className == "ApplicationFrameTitleBarWindow" },
            )
        ) }

    private val assignments = WindowAssignments().also { assignments ->
        assignments.addAll(
            listOf(
//                Assign.viewToWindow(1) { it.exeName == "chrome.exe" && !it.windowName.contains("Kalendarz Google") },
//                Assign.viewToWindow(2) { it.exeName == "idea64.exe" },
//                Assign.viewToWindow(3) { it.windowName == "WhatsApp" },
//                Assign.viewToWindow(3) { it.windowName == "Messenger" },
            )
        )
    }

    override fun reload() { }

    override fun createLayout(): Layout {
        val layout = TwoColumnLayout(0.55f)
//        layout = MinWindowSizeLayoutDecorator(1500, 800, twoColumnLayout)
//        return GapLayoutDecorator(20, layout)
        return layout
    }

    override fun getHotkeys(): List<Hotkey> {
        return listOf(
            // Switch view
            Hotkey("S-A-C-Y", SwitchView(0)),
            Hotkey("S-A-C-U", SwitchView(1)),
            Hotkey("S-A-C-I", SwitchView(2)),
            Hotkey("S-A-C-O", SwitchView(3)),
            Hotkey("S-A-C-P", SwitchView(4)),

            // Move window to view
            Hotkey("S-A-Y", MoveActiveWindowToView(0)),
            Hotkey("S-A-U", MoveActiveWindowToView(1)),
            Hotkey("S-A-I", MoveActiveWindowToView(2)),
            Hotkey("S-A-O", MoveActiveWindowToView(3)),
            Hotkey("S-A-P", MoveActiveWindowToView(4)),

            // Switch to previous view
            Hotkey("S-A-C-E", SwitchToPreviousView),

            // Window navigation
            Hotkey("S-A-C-L", MoveWindowRight),
            Hotkey("S-A-C-H", MoveWindowLeft),
            Hotkey("S-A-C-J", MoveWindowDown),
            Hotkey("S-A-C-K", MoveWindowUp),

            // Layout
            Hotkey("S-A-L", LayoutIncrease(0.05f)),
            Hotkey("S-A-H", LayoutDecrease(0.05f))
        )
    }

    override fun getFilteringRules(): FilteringRules = filteringRules

    override fun getAssignments(): WindowAssignments = assignments
}
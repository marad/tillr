package gh.marad.tiler.config.internal

import gh.marad.tiler.actions.*
import gh.marad.tiler.common.layout.GapLayoutDecorator
import gh.marad.tiler.common.layout.Layout
import gh.marad.tiler.common.layout.TwoColumnLayout
import gh.marad.tiler.config.ConfigFacade
import gh.marad.tiler.config.Hotkey

class SimpleConfig : ConfigFacade {
    override fun createLayout(): Layout {
        val twoColumnLayout = TwoColumnLayout(0.55f)
        return GapLayoutDecorator(20, twoColumnLayout)
    }

    override fun getHotkeys(): List<Hotkey> {
        return listOf(
            // Switch view
            Hotkey("S-A-C-U", SwitchView(0)),
            Hotkey("S-A-C-I", SwitchView(1)),
            Hotkey("S-A-C-O", SwitchView(2)),
            Hotkey("S-A-C-P", SwitchView(3)),

            // Move window to view
            Hotkey("S-A-U", MoveActiveWindowToView(0)),
            Hotkey("S-A-I", MoveActiveWindowToView(1)),
            Hotkey("S-A-O", MoveActiveWindowToView(2)),
            Hotkey("S-A-P", MoveActiveWindowToView(3)),

            // Switch to previous view
            Hotkey("S-A-C-E", SwitchToPreviousView),

            // Window navigation
            Hotkey("S-A-C-L", MoveWindowRight),
            Hotkey("S-A-C-H", MoveWindowLeft),
            Hotkey("S-A-C-J", MoveWindowDown),
            Hotkey("S-A-C-K", MoveWindowUp),

            // Layout
            Hotkey("S-A-L", LayoutIncrease(0.03f)),
            Hotkey("S-A-H", LayoutDecrease(0.03f))
        )
    }
}
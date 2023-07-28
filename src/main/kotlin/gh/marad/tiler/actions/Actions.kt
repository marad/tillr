package gh.marad.tiler.actions

sealed interface Action

data class LayoutIncrease(val amount: Float) : Action
data class LayoutDecrease(val amount: Float) : Action

data class SwitchView(val viewId: Int) : Action
object SwitchToPreviousView: Action
data class MoveActiveWindowToView(val viewId: Int) : Action

// window navigation
object MoveWindowLeft: Action
object MoveWindowRight: Action
object MoveWindowUp: Action
object MoveWindowDown: Action
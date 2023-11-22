package gh.marad.tiler.actions.internal

import gh.marad.tiler.actions.Action
import gh.marad.tiler.actions.ActionListener
import gh.marad.tiler.actions.ActionsFacade

class SimpleActions : ActionsFacade {
    private val actionListeners = mutableListOf<ActionListener>()

    override fun registerActionListener(actionListener: ActionListener) {
        actionListeners.add(actionListener)
    }

    override fun removeActionListener(actionListener: ActionListener) {
        actionListeners.remove(actionListener)
    }

    override suspend fun invokeAction(action: Action) {
        actionListeners.forEach { it.onAction(action) }
    }
}
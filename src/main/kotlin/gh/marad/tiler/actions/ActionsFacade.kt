package gh.marad.tiler.actions

import gh.marad.tiler.actions.internal.SimpleActions

interface ActionsFacade {
    fun registerActionListener(actionListener: ActionListener)
    fun removeActionListener(actionListener: ActionListener)

    suspend fun invokeAction(action: Action)

    companion object {
        fun createActions(): ActionsFacade = SimpleActions()
    }
}
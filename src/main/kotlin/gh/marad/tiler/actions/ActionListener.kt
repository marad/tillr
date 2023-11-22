package gh.marad.tiler.actions

interface ActionListener {
    suspend fun onAction(action: Action)
}
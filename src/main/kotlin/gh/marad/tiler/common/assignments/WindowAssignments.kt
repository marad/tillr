package gh.marad.tiler.common.assignments

import gh.marad.tiler.common.Window

class WindowAssignments {
    private val assignments: MutableList<Assignment> = mutableListOf()

    fun clear() {
        assignments.clear()
    }

    fun add(assignment: Assignment) {
        assignments.add(assignment)
    }

    fun getAssignmentForWindow(window: Window): Assignment? {
        return assignments.firstOrNull { it.matches(window) }
    }
}
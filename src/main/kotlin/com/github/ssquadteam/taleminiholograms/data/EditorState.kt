package com.github.ssquadteam.taleminiholograms.data

import kotlinx.serialization.Serializable

/**
 * State for the hologram editor UI with undo/redo support
 */
@Serializable
data class EditorState(
    val hologramName: String,
    val lines: List<String>,
    val selectedLineIndex: Int = 0,
    val undoStack: List<List<String>> = emptyList(),
    val redoStack: List<List<String>> = emptyList()
) {

    val canUndo: Boolean get() = undoStack.isNotEmpty()

    val canRedo: Boolean get() = redoStack.isNotEmpty()

    fun pushUndo(): EditorState = copy(
        undoStack = undoStack + listOf(lines),
        redoStack = emptyList()
    )

    fun undo(): EditorState? {
        if (undoStack.isEmpty()) return null
        val previousLines = undoStack.last()
        return copy(
            lines = previousLines,
            undoStack = undoStack.dropLast(1),
            redoStack = redoStack + listOf(lines),
            selectedLineIndex = selectedLineIndex.coerceIn(0, previousLines.size - 1)
        )
    }

    fun redo(): EditorState? {
        if (redoStack.isEmpty()) return null
        val nextLines = redoStack.last()
        return copy(
            lines = nextLines,
            undoStack = undoStack + listOf(lines),
            redoStack = redoStack.dropLast(1),
            selectedLineIndex = selectedLineIndex.coerceIn(0, nextLines.size - 1)
        )
    }

    fun updateLines(newLines: List<String>): EditorState = pushUndo().copy(lines = newLines)

    fun updateLine(index: Int, newText: String): EditorState {
        if (index < 0 || index >= lines.size) return this
        val newLines = lines.toMutableList()
        newLines[index] = newText
        return pushUndo().copy(lines = newLines)
    }

    fun addLine(text: String = ""): EditorState = pushUndo().copy(
        lines = lines + text,
        selectedLineIndex = lines.size
    )

    fun removeLine(): EditorState {
        if (lines.size <= 1) return this
        val newLines = lines.filterIndexed { i, _ -> i != selectedLineIndex }
        return pushUndo().copy(
            lines = newLines,
            selectedLineIndex = (selectedLineIndex - 1).coerceAtLeast(0)
        )
    }

    fun moveLineUp(): EditorState {
        if (selectedLineIndex <= 0) return this
        val newLines = lines.toMutableList()
        val temp = newLines[selectedLineIndex]
        newLines[selectedLineIndex] = newLines[selectedLineIndex - 1]
        newLines[selectedLineIndex - 1] = temp
        return pushUndo().copy(
            lines = newLines,
            selectedLineIndex = selectedLineIndex - 1
        )
    }

    fun moveLineDown(): EditorState {
        if (selectedLineIndex >= lines.size - 1) return this
        val newLines = lines.toMutableList()
        val temp = newLines[selectedLineIndex]
        newLines[selectedLineIndex] = newLines[selectedLineIndex + 1]
        newLines[selectedLineIndex + 1] = temp
        return pushUndo().copy(
            lines = newLines,
            selectedLineIndex = selectedLineIndex + 1
        )
    }

    fun selectLine(index: Int): EditorState =
        copy(selectedLineIndex = index.coerceIn(0, (lines.size - 1).coerceAtLeast(0)))
}

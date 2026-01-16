package com.github.ssquadteam.taleminiholograms.ui

import com.github.ssquadteam.talelib.player.send
import com.github.ssquadteam.talelib.player.uniqueId
import com.github.ssquadteam.talelib.ui.command.dsl
import com.github.ssquadteam.talelib.ui.event.ParsedEventData
import com.github.ssquadteam.talelib.ui.event.dsl
import com.github.ssquadteam.talelib.ui.page.TalePage
import com.github.ssquadteam.taleminiholograms.TaleMiniHolograms
import com.github.ssquadteam.taleminiholograms.data.EditorState
import com.github.ssquadteam.taleminiholograms.util.mini
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder
import com.hypixel.hytale.server.core.universe.PlayerRef
import java.util.concurrent.ConcurrentHashMap

/**
 * UI Page handler for the hologram editor.
 * Line-based editor with add/remove line buttons.
 */
object HologramEditorPage {

    private const val UI_PATH = "hologram_editor.ui"
    private const val VISIBLE_LINES = 6  // Number of visible slots in the UI

    private val playerPlugins = ConcurrentHashMap<PlayerRef, TaleMiniHolograms>()

    private val playerLines = ConcurrentHashMap<PlayerRef, MutableList<String>>()

    private val playerSelectedIndex = ConcurrentHashMap<PlayerRef, Int>()

    private val playerScrollOffset = ConcurrentHashMap<PlayerRef, Int>()

    private val page = object : TalePage("hologram_editor", UI_PATH) {

        override fun onOpen(player: PlayerRef) {
        }

        override fun onClose(player: PlayerRef) {
            val plugin = playerPlugins.remove(player) ?: return
            playerLines.remove(player)
            playerSelectedIndex.remove(player)
            playerScrollOffset.remove(player)
            plugin.editorManager.endSession(player.uniqueId, save = false)
        }

        override fun onBuild(player: PlayerRef, builder: UICommandBuilder, eventBuilder: UIEventBuilder) {
            builder.append(UI_PATH)

            val plugin = playerPlugins[player] ?: return
            val state = plugin.editorManager.getSession(player.uniqueId) ?: return

            val lines = state.lines.toMutableList()
            if (lines.isEmpty()) {
                lines.add("")
            }
            playerLines[player] = lines
            playerSelectedIndex[player] = 0
            playerScrollOffset[player] = 0

            val totalPages = ((lines.size - 1) / VISIBLE_LINES) + 1

            builder.dsl {
                text("title", "Editing: ${state.hologramName}")
                text("lineCountLabel", "Lines: ${lines.size}")
                text("pageLabel", "1/$totalPages")
            }

            builder.dsl {
                for (slot in 0 until VISIBLE_LINES) {
                    val lineIndex = slot
                    if (lineIndex < lines.size) {
                        visible("line$slot", true)
                        value("line${slot}text", lines[lineIndex])
                        text("line${slot}num", "${lineIndex + 1}.")
                        if (lineIndex == 0) {
                            set("#line${slot}sel.Background", "#3b82f6")
                        } else {
                            set("#line${slot}sel.Background", "#2b3542")
                        }
                    } else {
                        visible("line$slot", false)
                    }
                }
            }

            eventBuilder.dsl {
                onClickButtons("saveBtn", "cancelBtn", "closeBtn", "addLineBtn", "removeLineBtn", "scrollUpBtn", "scrollDownBtn")
                for (slot in 0 until VISIBLE_LINES) {
                    onValueChanged("line${slot}text", "lineChanged$slot")
                    onFocusGained("line${slot}text", "lineSelected$slot")
                }
            }
        }

        override fun onParsedEvent(player: PlayerRef, event: ParsedEventData) {
            handleEvent(player, event)
        }
    }

    fun open(player: PlayerRef, state: EditorState, plugin: TaleMiniHolograms) {
        playerPlugins[player] = plugin
        page.open(player)
    }

    private fun handleEvent(player: PlayerRef, event: ParsedEventData) {
        val plugin = playerPlugins[player] ?: return
        val action = event.action ?: return
        val scrollOffset = playerScrollOffset[player] ?: 0

        when {
            action.startsWith("lineSelected") -> {
                val slot = action.removePrefix("lineSelected").toIntOrNull() ?: return
                val lineIndex = scrollOffset + slot
                updateSelection(player, lineIndex)
            }

            action.startsWith("lineChanged") -> {
                val slot = action.removePrefix("lineChanged").toIntOrNull() ?: return
                val lineIndex = scrollOffset + slot
                val newValue = event.value
                if (newValue != null) {
                    val lines = playerLines[player] ?: return
                    if (lineIndex in lines.indices) {
                        lines[lineIndex] = newValue
                    }
                    playerSelectedIndex[player] = lineIndex
                }
            }

            action == "addLineBtn" -> {
                addLine(player)
            }

            action == "removeLineBtn" -> {
                removeLine(player)
            }

            action == "scrollUpBtn" -> {
                scrollUp(player)
            }

            action == "scrollDownBtn" -> {
                scrollDown(player)
            }

            action == "saveBtn" -> {
                saveAndClose(player, plugin)
            }

            action == "cancelBtn" || action == "closeBtn" -> {
                cancelAndClose(player, plugin)
            }
        }
    }

    /**
     * Scroll up (show previous lines)
     */
    private fun scrollUp(player: PlayerRef) {
        val currentOffset = playerScrollOffset[player] ?: 0
        if (currentOffset <= 0) return

        val newOffset = (currentOffset - VISIBLE_LINES).coerceAtLeast(0)
        playerScrollOffset[player] = newOffset
        refreshLinesUI(player)
    }

    /**
     * Scroll down (show next lines)
     */
    private fun scrollDown(player: PlayerRef) {
        val lines = playerLines[player] ?: return
        val currentOffset = playerScrollOffset[player] ?: 0
        val maxOffset = (lines.size - VISIBLE_LINES).coerceAtLeast(0)

        if (currentOffset >= maxOffset) return

        val newOffset = (currentOffset + VISIBLE_LINES).coerceAtMost(maxOffset)
        playerScrollOffset[player] = newOffset
        refreshLinesUI(player)
    }

    private fun updateSelection(player: PlayerRef, newIndex: Int) {
        val lines = playerLines[player] ?: return
        val scrollOffset = playerScrollOffset[player] ?: 0
        val oldIndex = playerSelectedIndex[player] ?: 0

        if (newIndex < 0 || newIndex >= lines.size) return

        playerSelectedIndex[player] = newIndex

        page.update(player) {
            val oldSlot = oldIndex - scrollOffset
            if (oldSlot in 0 until VISIBLE_LINES) {
                set("#line${oldSlot}sel.Background", "#2b3542")
            }
            val newSlot = newIndex - scrollOffset
            if (newSlot in 0 until VISIBLE_LINES) {
                set("#line${newSlot}sel.Background", "#3b82f6")
            }
        }
    }

    private fun addLine(player: PlayerRef) {
        val plugin = playerPlugins[player] ?: return
        val lines = playerLines[player] ?: return
        val selectedIndex = playerSelectedIndex[player] ?: 0

        val maxLines = plugin.hologramManager.config.maxLinesPerHologram
        if (maxLines > 0 && lines.size >= maxLines) {
            player.send("<red>Maximum $maxLines lines allowed!".mini())
            return
        }

        val insertIndex = selectedIndex + 1
        lines.add(insertIndex, "")

        val scrollOffset = playerScrollOffset[player] ?: 0
        if (insertIndex >= scrollOffset + VISIBLE_LINES) {
            playerScrollOffset[player] = (insertIndex - VISIBLE_LINES + 1).coerceAtLeast(0)
        }

        playerSelectedIndex[player] = insertIndex

        refreshLinesUI(player)
    }

    private fun removeLine(player: PlayerRef) {
        val lines = playerLines[player] ?: return
        val selectedIndex = playerSelectedIndex[player] ?: 0

        if (lines.size <= 1) {
            player.send("<red>Cannot remove the last line!".mini())
            return
        }

        if (selectedIndex !in lines.indices) return

        lines.removeAt(selectedIndex)

        val newIndex = if (selectedIndex >= lines.size) lines.size - 1 else selectedIndex
        playerSelectedIndex[player] = newIndex

        val scrollOffset = playerScrollOffset[player] ?: 0
        val maxOffset = (lines.size - VISIBLE_LINES).coerceAtLeast(0)
        if (scrollOffset > maxOffset) {
            playerScrollOffset[player] = maxOffset
        }

        refreshLinesUI(player)
    }

    private fun refreshLinesUI(player: PlayerRef) {
        val lines = playerLines[player] ?: return
        val selectedIndex = playerSelectedIndex[player] ?: 0
        val scrollOffset = playerScrollOffset[player] ?: 0

        val currentPage = (scrollOffset / VISIBLE_LINES) + 1
        val totalPages = ((lines.size - 1) / VISIBLE_LINES) + 1

        page.update(player) {
            text("lineCountLabel", "Lines: ${lines.size}")
            text("pageLabel", "$currentPage/$totalPages")

            for (slot in 0 until VISIBLE_LINES) {
                val lineIndex = scrollOffset + slot
                if (lineIndex < lines.size) {
                    visible("line$slot", true)
                    value("line${slot}text", lines[lineIndex])
                    text("line${slot}num", "${lineIndex + 1}.")
                    if (lineIndex == selectedIndex) {
                        set("#line${slot}sel.Background", "#3b82f6")
                    } else {
                        set("#line${slot}sel.Background", "#2b3542")
                    }
                } else {
                    visible("line$slot", false)
                }
            }
        }
    }

    private fun saveAndClose(player: PlayerRef, plugin: TaleMiniHolograms) {
        val lines = playerLines[player] ?: mutableListOf()
        val filteredLines = lines.filter { it.isNotBlank() }

        if (filteredLines.isEmpty()) {
            player.send("<red>Hologram cannot be empty! Add at least one line with text.".mini())
            return
        }

        val state = plugin.editorManager.getSession(player.uniqueId)
        if (state != null) {
            val newState = state.copy(lines = filteredLines)
            plugin.editorManager.updateSession(player.uniqueId, newState)
        }

        plugin.editorManager.endSession(player.uniqueId, save = true)
        playerPlugins.remove(player)
        playerLines.remove(player)
        playerSelectedIndex.remove(player)
        playerScrollOffset.remove(player)
        page.close(player)
        player.send("<green>Hologram saved!".mini())
    }

    private fun cancelAndClose(player: PlayerRef, plugin: TaleMiniHolograms) {
        plugin.editorManager.endSession(player.uniqueId, save = false)
        playerPlugins.remove(player)
        playerLines.remove(player)
        playerSelectedIndex.remove(player)
        playerScrollOffset.remove(player)
        page.close(player)
        player.send("<yellow>Editor closed without saving.".mini())
    }
}

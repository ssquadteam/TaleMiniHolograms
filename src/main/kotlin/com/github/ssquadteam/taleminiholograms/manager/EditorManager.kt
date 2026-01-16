package com.github.ssquadteam.taleminiholograms.manager

import com.github.ssquadteam.taleminiholograms.TaleMiniHolograms
import com.github.ssquadteam.taleminiholograms.data.EditorState
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages active editor sessions for players.
 * Each player can have one active editor session at a time.
 */
class EditorManager(private val plugin: TaleMiniHolograms) {

    private val activeSessions = ConcurrentHashMap<UUID, EditorState>()

    fun startSession(playerUuid: UUID, hologramName: String): EditorState? {
        val data = plugin.hologramManager.get(hologramName) ?: return null

        val state = EditorState(
            hologramName = hologramName,
            lines = data.lines
        )

        activeSessions[playerUuid] = state
        return state
    }

    fun getSession(playerUuid: UUID): EditorState? = activeSessions[playerUuid]

    fun updateSession(playerUuid: UUID, newState: EditorState) {
        activeSessions[playerUuid] = newState
    }

    fun endSession(playerUuid: UUID, save: Boolean = false): EditorState? {
        val state = activeSessions.remove(playerUuid) ?: return null

        if (save) {
            plugin.hologramManager.updateLines(state.hologramName, state.lines)
        }

        return state
    }

    fun hasActiveSession(playerUuid: UUID): Boolean = activeSessions.containsKey(playerUuid)

    fun getAllSessions(): Map<UUID, EditorState> = activeSessions.toMap()

    fun endAllSessions() {
        activeSessions.keys.toList().forEach { playerUuid ->
            endSession(playerUuid, save = false)
        }
    }
}

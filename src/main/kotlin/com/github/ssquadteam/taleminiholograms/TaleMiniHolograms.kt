package com.github.ssquadteam.taleminiholograms

import com.github.ssquadteam.talelib.TalePlugin
import com.github.ssquadteam.talelib.hologram.HologramManager as TaleHologramManager
import com.github.ssquadteam.taleminiholograms.command.TMHCommand
import com.github.ssquadteam.taleminiholograms.manager.EditorManager
import com.github.ssquadteam.taleminiholograms.manager.HologramManager
import com.hypixel.hytale.server.core.plugin.JavaPluginInit
import kotlin.time.Duration.Companion.seconds

class TaleMiniHolograms(init: JavaPluginInit) : TalePlugin(init) {

    companion object {
        lateinit var instance: TaleMiniHolograms
            private set
    }

    val version = "1.0.1"

    lateinit var hologramManager: HologramManager
        private set

    lateinit var editorManager: EditorManager
        private set

    private var autoSaveTaskId: Long = -1

    override fun onSetup() {
        instance = this
        info("TaleMiniHolograms v$version setting up...")
    }

    override fun onStart() {
        info("TaleMiniHolograms v$version starting...")

        hologramManager = HologramManager(this)
        editorManager = EditorManager(this)

        hologramManager.loadConfig()
        hologramManager.loadStore()

        taleCommands.register(TMHCommand(this))

        // Clean up any orphaned holograms from previous session (crash/force-stop)
        val orphanedCount = TaleHologramManager.count()
        if (orphanedCount > 0) {
            info("Cleaning up $orphanedCount orphaned holograms from previous session...")
            TaleHologramManager.removeAll()
        }

        hologramManager.spawnAll()

        setupAutoSave()

        info("TaleMiniHolograms started! Loaded ${hologramManager.store.count()} holograms.")
    }

    override fun onShutdown() {
        info("TaleMiniHolograms shutting down...")

        if (autoSaveTaskId >= 0) {
            taleScheduler.cancel(autoSaveTaskId)
        }

        editorManager.endAllSessions()

        hologramManager.saveStore()
        hologramManager.despawnAll()

        info("TaleMiniHolograms disabled!")
    }

    private fun setupAutoSave() {
        val intervalSeconds = hologramManager.config.autoSaveIntervalSeconds
        if (intervalSeconds > 0) {
            autoSaveTaskId = taleScheduler.repeat(
                interval = intervalSeconds.seconds,
                initialDelay = intervalSeconds.seconds
            ) {
                hologramManager.saveStore()
                debug("Auto-saved holograms.")
            }
        }
    }
}

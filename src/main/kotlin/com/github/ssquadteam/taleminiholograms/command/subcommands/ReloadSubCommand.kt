package com.github.ssquadteam.taleminiholograms.command.subcommands

import com.github.ssquadteam.talelib.command.TaleCommand
import com.github.ssquadteam.talelib.command.TaleContext
import com.github.ssquadteam.taleminiholograms.TaleMiniHolograms
import com.github.ssquadteam.taleminiholograms.util.*

/**
 * /tmh reload - Reload configuration and respawn all holograms
 */
class ReloadSubCommand(private val plugin: TaleMiniHolograms) : TaleCommand("reload", "Reload configuration and holograms") {

    override fun onExecute(ctx: TaleContext) {
        val playerRef = ctx.playerRef

        if (playerRef != null && !playerRef.hasPermission("tmh.admin")) {
            ctx.error("You don't have permission to reload the plugin!")
            return
        }

        ctx.replyPrefixed("<gray>Reloading...")

        try {
            plugin.hologramManager.reload()
            ctx.replyPrefixed("<green>Configuration and holograms reloaded!")
            ctx.info("Loaded ${plugin.hologramManager.store.count()} holograms.")
        } catch (e: Exception) {
            ctx.error("Failed to reload: ${e.message}")
            plugin.error("Reload failed: ${e.message}")
        }
    }
}

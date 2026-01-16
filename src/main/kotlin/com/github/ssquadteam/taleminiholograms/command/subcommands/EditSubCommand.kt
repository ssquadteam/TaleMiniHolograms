package com.github.ssquadteam.taleminiholograms.command.subcommands

import com.github.ssquadteam.talelib.command.TaleCommand
import com.github.ssquadteam.talelib.command.TaleContext
import com.github.ssquadteam.talelib.player.uniqueId
import com.github.ssquadteam.taleminiholograms.TaleMiniHolograms
import com.github.ssquadteam.taleminiholograms.ui.HologramEditorPage
import com.github.ssquadteam.taleminiholograms.util.*

/**
 * /tmh edit <name> - Open the hologram editor UI
 */
class EditSubCommand(private val plugin: TaleMiniHolograms) : TaleCommand("edit", "Open the hologram editor UI") {

    private val nameArg = stringArg("name", "Name of the hologram to edit")

    override fun onExecute(ctx: TaleContext) {
        val player = ctx.requirePlayer("You must be a player to edit holograms.") ?: return
        val playerRef = ctx.playerRef ?: return

        if (!playerRef.hasPermission("tmh.edit")) {
            ctx.error("You don't have permission to edit holograms!")
            return
        }

        val name = ctx.get(nameArg)

        if (plugin.hologramManager.get(name) == null) {
            ctx.error("Hologram '$name' not found!")
            ctx.info("Use <yellow>/tmh list</yellow> to see all holograms.")
            return
        }

        if (plugin.editorManager.hasActiveSession(playerRef.uniqueId)) {
            ctx.warning("You already have an editor open. Please close it first.")
            return
        }

        val state = plugin.editorManager.startSession(playerRef.uniqueId, name)
        if (state == null) {
            ctx.error("Failed to open editor. Please try again.")
            return
        }

        val world = player.world
        if (world == null) {
            ctx.error("Could not determine your world!")
            return
        }

        world.execute {
            HologramEditorPage.open(playerRef, state, plugin)
            ctx.replyPrefixed("<green>Opening editor for <yellow>$name</yellow>...")
        }
    }
}

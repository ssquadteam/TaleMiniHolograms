package com.github.ssquadteam.taleminiholograms.command.subcommands

import com.github.ssquadteam.talelib.command.TaleCommand
import com.github.ssquadteam.talelib.command.TaleContext
import com.github.ssquadteam.taleminiholograms.TaleMiniHolograms
import com.github.ssquadteam.taleminiholograms.util.*

/**
 * /tmh clone <name> [newname] - Clone a hologram
 * If no new name is provided, generates one automatically (name2, name3, etc.)
 */
class CloneSubCommand(private val plugin: TaleMiniHolograms) : TaleCommand("clone", "Clone a hologram") {

    private val sourceNameArg = stringArg("source", "Name of the hologram to clone")
    private val targetNameArg = optionalString("newname", "Name for the cloned hologram (optional)")

    init {
        aliases("copy", "duplicate")
    }

    override fun onExecute(ctx: TaleContext) {
        val playerRef = ctx.playerRef

        if (playerRef != null && !playerRef.hasPermission("tmh.clone")) {
            ctx.error("You don't have permission to clone holograms!")
            return
        }

        val sourceName = ctx.get(sourceNameArg)
        val targetName = ctx.get(targetNameArg)

        if (plugin.hologramManager.get(sourceName) == null) {
            ctx.error("Hologram '$sourceName' not found!")
            return
        }

        if (targetName != null && plugin.hologramManager.get(targetName) != null) {
            ctx.error("A hologram named '$targetName' already exists!")
            return
        }

        val newName = plugin.hologramManager.clone(sourceName, targetName)

        if (newName != null) {
            ctx.replyPrefixed("<green>Cloned <yellow>$sourceName</yellow> to <yellow>$newName</yellow>.")
            ctx.info("Edit it with <yellow>/tmh edit $newName")
        } else {
            ctx.error("Failed to clone hologram. Please try again.")
        }
    }
}

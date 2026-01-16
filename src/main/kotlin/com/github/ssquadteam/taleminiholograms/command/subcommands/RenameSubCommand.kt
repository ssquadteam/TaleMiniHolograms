package com.github.ssquadteam.taleminiholograms.command.subcommands

import com.github.ssquadteam.talelib.command.TaleCommand
import com.github.ssquadteam.talelib.command.TaleContext
import com.github.ssquadteam.taleminiholograms.TaleMiniHolograms
import com.github.ssquadteam.taleminiholograms.util.*

/**
 * /tmh rename <old> <new> - Rename a hologram
 */
class RenameSubCommand(private val plugin: TaleMiniHolograms) : TaleCommand("rename", "Rename a hologram") {

    private val oldNameArg = stringArg("oldname", "Current name of the hologram")
    private val newNameArg = stringArg("newname", "New name for the hologram")

    override fun onExecute(ctx: TaleContext) {
        val playerRef = ctx.playerRef

        if (playerRef != null && !playerRef.hasPermission("tmh.rename")) {
            ctx.error("You don't have permission to rename holograms!")
            return
        }

        val oldName = ctx.get(oldNameArg)
        val newName = ctx.get(newNameArg)

        if (newName.isBlank()) {
            ctx.error("New name cannot be empty!")
            return
        }

        if (newName.contains(" ")) {
            ctx.error("Hologram name cannot contain spaces!")
            return
        }

        if (plugin.hologramManager.get(oldName) == null) {
            ctx.error("Hologram '$oldName' not found!")
            return
        }

        if (plugin.hologramManager.get(newName) != null) {
            ctx.error("A hologram named '$newName' already exists!")
            return
        }

        val success = plugin.hologramManager.rename(oldName, newName)

        if (success) {
            ctx.replyPrefixed("<green>Renamed <yellow>$oldName</yellow> to <yellow>$newName</yellow>.")
        } else {
            ctx.error("Failed to rename hologram. Please try again.")
        }
    }
}

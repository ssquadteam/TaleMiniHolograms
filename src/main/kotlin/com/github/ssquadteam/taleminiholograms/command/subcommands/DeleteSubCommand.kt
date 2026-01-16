package com.github.ssquadteam.taleminiholograms.command.subcommands

import com.github.ssquadteam.talelib.command.TaleCommand
import com.github.ssquadteam.talelib.command.TaleContext
import com.github.ssquadteam.taleminiholograms.TaleMiniHolograms
import com.github.ssquadteam.taleminiholograms.util.*

/**
 * /tmh delete <name> - Delete a hologram
 */
class DeleteSubCommand(private val plugin: TaleMiniHolograms) : TaleCommand("delete", "Delete a hologram") {

    private val nameArg = stringArg("name", "Name of the hologram to delete")

    init {
        aliases("remove", "del")
    }

    override fun onExecute(ctx: TaleContext) {
        val playerRef = ctx.playerRef

        if (playerRef != null && !playerRef.hasPermission("tmh.delete")) {
            ctx.error("You don't have permission to delete holograms!")
            return
        }

        val name = ctx.get(nameArg)

        if (plugin.hologramManager.get(name) == null) {
            ctx.error("Hologram '$name' not found!")
            return
        }

        val success = plugin.hologramManager.delete(name)

        if (success) {
            ctx.replyPrefixed("<green>Deleted hologram <yellow>$name</yellow>.")
        } else {
            ctx.error("Failed to delete hologram. Please try again.")
        }
    }
}

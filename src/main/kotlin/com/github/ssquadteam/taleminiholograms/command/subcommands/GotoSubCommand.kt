package com.github.ssquadteam.taleminiholograms.command.subcommands

import com.github.ssquadteam.talelib.command.TaleCommand
import com.github.ssquadteam.talelib.command.TaleContext
import com.github.ssquadteam.talelib.teleport.teleport
import com.github.ssquadteam.taleminiholograms.TaleMiniHolograms
import com.github.ssquadteam.taleminiholograms.util.*
import com.github.ssquadteam.talelib.world.findWorldByName

/**
 * /tmh goto <name> - Teleport to a hologram
 */
class GotoSubCommand(private val plugin: TaleMiniHolograms) : TaleCommand("goto", "Teleport to a hologram") {

    private val nameArg = stringArg("name", "Name of the hologram to teleport to")

    init {
        aliases("teleport", "tpto")
    }

    override fun onExecute(ctx: TaleContext) {
        ctx.requirePlayer("You must be a player to teleport.") ?: return
        val playerRef = ctx.playerRef ?: return

        if (!playerRef.hasPermission("tmh.goto")) {
            ctx.error("You don't have permission to teleport to holograms!")
            return
        }

        val name = ctx.get(nameArg)

        val data = plugin.hologramManager.get(name)
        if (data == null) {
            ctx.error("Hologram '$name' not found!")
            return
        }

        val world = findWorldByName(data.worldUuid)
        if (world == null) {
            ctx.error("Could not find the world for this hologram!")
            return
        }

        playerRef.teleport(data.x, data.y - 1.0, data.z)

        ctx.replyPrefixed("<green>Teleported to hologram <yellow>$name</yellow>.")
    }
}

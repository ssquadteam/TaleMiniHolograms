package com.github.ssquadteam.taleminiholograms.command.subcommands

import com.github.ssquadteam.talelib.command.TaleCommand
import com.github.ssquadteam.talelib.command.TaleContext
import com.github.ssquadteam.taleminiholograms.TaleMiniHolograms
import com.github.ssquadteam.taleminiholograms.util.*

/**
 * /tmh movehere <name> - Move a hologram to the player's location
 */
class MoveHereSubCommand(private val plugin: TaleMiniHolograms) : TaleCommand("movehere", "Move a hologram to your location") {

    private val nameArg = stringArg("name", "Name of the hologram to move")

    init {
        aliases("move", "tp")
    }

    override fun onExecute(ctx: TaleContext) {
        val player = ctx.requirePlayer("You must be a player to move holograms.") ?: return
        val playerRef = ctx.playerRef ?: return

        if (!playerRef.hasPermission("tmh.move")) {
            ctx.error("You don't have permission to move holograms!")
            return
        }

        val name = ctx.get(nameArg)

        if (plugin.hologramManager.get(name) == null) {
            ctx.error("Hologram '$name' not found!")
            return
        }

        val world = player.world
        if (world == null) {
            ctx.error("Could not determine your world!")
            return
        }

        world.execute {
            val position = playerRef.getPlayerPosition()
            if (position == null) {
                ctx.error("Could not determine your position!")
                return@execute
            }

            val success = plugin.hologramManager.move(
                name = name,
                worldUuid = world.getWorldId(),
                x = position.x,
                y = position.y + 1.5,
                z = position.z
            )

            if (success) {
                ctx.replyPrefixed("<green>Moved hologram <yellow>$name</yellow> to your location.")
            } else {
                ctx.error("Failed to move hologram. Please try again.")
            }
        }
    }
}

package com.github.ssquadteam.taleminiholograms.command.subcommands

import com.github.ssquadteam.talelib.command.TaleCommand
import com.github.ssquadteam.talelib.command.TaleContext
import com.github.ssquadteam.talelib.player.uniqueId
import com.github.ssquadteam.taleminiholograms.TaleMiniHolograms
import com.github.ssquadteam.taleminiholograms.util.*

/**
 * /tmh create <name> - Create a new hologram at the player's location
 */
class CreateSubCommand(private val plugin: TaleMiniHolograms) : TaleCommand("create", "Create a new hologram at your location") {

    private val nameArg = stringArg("name", "Name for the hologram")

    override fun onExecute(ctx: TaleContext) {
        val player = ctx.requirePlayer("You must be a player to create holograms.") ?: return
        val playerRef = ctx.playerRef ?: return

        if (!playerRef.hasPermission("tmh.create")) {
            ctx.error("You don't have permission to create holograms!")
            return
        }

        val name = ctx.get(nameArg)

        if (name.isBlank()) {
            ctx.error("Hologram name cannot be empty!")
            return
        }

        if (name.contains(" ")) {
            ctx.error("Hologram name cannot contain spaces!")
            return
        }

        if (plugin.hologramManager.get(name) != null) {
            ctx.error("A hologram named '$name' already exists!")
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

            val success = plugin.hologramManager.create(
                name = name,
                worldUuid = world.getWorldId(),
                x = position.x,
                y = position.y + 1.5,
                z = position.z,
                creatorUuid = playerRef.uniqueId.toString()
            )

            if (success) {
                ctx.replyPrefixed("<green>Created hologram <yellow>$name</yellow>!")
                ctx.replyPrefixed("<gray>Edit it with <yellow>/tmh edit $name")
            } else {
                ctx.error("Failed to create hologram. Please try again.")
            }
        }
    }
}

package com.github.ssquadteam.taleminiholograms.command.subcommands

import com.github.ssquadteam.talelib.command.TaleCommand
import com.github.ssquadteam.talelib.command.TaleContext
import com.github.ssquadteam.taleminiholograms.TaleMiniHolograms
import com.github.ssquadteam.taleminiholograms.util.*

/**
 * /tmh help - Show help for all commands
 */
class HelpSubCommand(private val plugin: TaleMiniHolograms) : TaleCommand("help", "Show command help") {

    init {
        aliases("?")
    }

    override fun onExecute(ctx: TaleContext) {
        ctx.replyMini("")
        ctx.replyMini("$PREFIX<gray>v${plugin.version} - <white>Command Help")
        ctx.replyMini("")
        ctx.replyMini("<gold>Basic Commands:")
        ctx.replyMini("  <yellow>/tmh create <name></yellow> <dark_gray>-</dark_gray> <gray>Create a hologram at your location")
        ctx.replyMini("  <yellow>/tmh edit <name></yellow> <dark_gray>-</dark_gray> <gray>Open the hologram editor UI")
        ctx.replyMini("  <yellow>/tmh delete <name></yellow> <dark_gray>-</dark_gray> <gray>Delete a hologram")
        ctx.replyMini("  <yellow>/tmh list</yellow> <dark_gray>-</dark_gray> <gray>List all holograms")
        ctx.replyMini("")
        ctx.replyMini("<gold>Position Commands:")
        ctx.replyMini("  <yellow>/tmh movehere <name></yellow> <dark_gray>-</dark_gray> <gray>Move hologram to your location")
        ctx.replyMini("  <yellow>/tmh goto <name></yellow> <dark_gray>-</dark_gray> <gray>Teleport to a hologram")
        ctx.replyMini("")
        ctx.replyMini("<gold>Advanced Commands:")
        ctx.replyMini("  <yellow>/tmh clone <name> [newname]</yellow> <dark_gray>-</dark_gray> <gray>Clone a hologram")
        ctx.replyMini("  <yellow>/tmh rename <old> <new></yellow> <dark_gray>-</dark_gray> <gray>Rename a hologram")
        ctx.replyMini("  <yellow>/tmh reload</yellow> <dark_gray>-</dark_gray> <gray>Reload configuration")
        ctx.replyMini("")
        ctx.replyMini("<gold>Permissions:")
        ctx.replyMini("  <gray>tmh.create, tmh.edit, tmh.delete, tmh.list")
        ctx.replyMini("  <gray>tmh.move, tmh.goto, tmh.clone, tmh.rename")
        ctx.replyMini("  <gray>tmh.admin (reload), tmh.* (all)")
        ctx.replyMini("")
    }
}

package com.github.ssquadteam.taleminiholograms.command.subcommands

import com.github.ssquadteam.talelib.command.TaleCommand
import com.github.ssquadteam.talelib.command.TaleContext
import com.github.ssquadteam.taleminiholograms.TaleMiniHolograms
import com.github.ssquadteam.taleminiholograms.util.*

/**
 * /tmh list - List all holograms
 */
class ListSubCommand(private val plugin: TaleMiniHolograms) : TaleCommand("list", "List all holograms") {

    init {
        aliases("ls")
    }

    override fun onExecute(ctx: TaleContext) {
        val playerRef = ctx.playerRef

        if (playerRef != null && !playerRef.hasPermission("tmh.list")) {
            ctx.error("You don't have permission to list holograms!")
            return
        }

        val store = plugin.hologramManager.store

        if (store.count() == 0) {
            ctx.replyPrefixed("<gray>No holograms have been created yet.")
            ctx.info("Create one with <yellow>/tmh create <name>")
            return
        }

        ctx.replyMini("")
        ctx.replyPrefixed("<gold>Holograms <gray>(${store.count()}):")
        ctx.replyMini("")

        store.holograms.values.forEach { data ->
            val x = String.format("%.1f", data.x)
            val y = String.format("%.1f", data.y)
            val z = String.format("%.1f", data.z)
            val lines = data.lines.size

            ctx.replyMini("  <yellow>${data.name}</yellow> <dark_gray>-</dark_gray> <gray>$lines line${if (lines != 1) "s" else ""} at ($x, $y, $z)")
        }

        ctx.replyMini("")
    }
}

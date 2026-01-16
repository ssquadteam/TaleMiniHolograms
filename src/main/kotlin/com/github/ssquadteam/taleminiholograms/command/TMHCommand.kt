package com.github.ssquadteam.taleminiholograms.command

import com.github.ssquadteam.talelib.command.TaleCommand
import com.github.ssquadteam.talelib.command.TaleContext
import com.github.ssquadteam.taleminiholograms.TaleMiniHolograms
import com.github.ssquadteam.taleminiholograms.command.subcommands.*
import com.github.ssquadteam.taleminiholograms.util.PREFIX
import com.github.ssquadteam.taleminiholograms.util.replyMini

/**
 * Root command for TaleMiniHolograms: /tmh
 */
class TMHCommand(private val plugin: TaleMiniHolograms) : TaleCommand("tmh", "TaleMiniHolograms commands") {

    init {
        aliases("taleminiholograms", "miniholo", "holo")

        subCommand(CreateSubCommand(plugin))
        subCommand(EditSubCommand(plugin))
        subCommand(DeleteSubCommand(plugin))
        subCommand(ListSubCommand(plugin))
        subCommand(MoveHereSubCommand(plugin))
        subCommand(GotoSubCommand(plugin))
        subCommand(CloneSubCommand(plugin))
        subCommand(RenameSubCommand(plugin))
        subCommand(ReloadSubCommand(plugin))
        subCommand(HelpSubCommand(plugin))
    }

    override fun onExecute(ctx: TaleContext) {
        ctx.replyMini("")
        ctx.replyMini("$PREFIX<gray>v${plugin.version}")
        ctx.replyMini("")
        ctx.replyMini("<gray>Use <yellow>/tmh help</yellow> for a list of commands.")
        ctx.replyMini("")
    }
}

package com.github.ssquadteam.taleminiholograms.util

import com.github.ssquadteam.hytaleminiformat.MiniFormat
import com.github.ssquadteam.talelib.command.TaleContext
import com.github.ssquadteam.talelib.entity.getPosition
import com.hypixel.hytale.math.vector.Vector3d
import com.hypixel.hytale.server.core.HytaleServer
import com.hypixel.hytale.server.core.Message
import com.hypixel.hytale.server.core.entity.entities.Player
import com.hypixel.hytale.server.core.universe.PlayerRef
import com.hypixel.hytale.server.core.universe.world.World

fun String.mini(): Message = MiniFormat.parse(this)

fun TaleContext.replyMini(message: String) {
    reply(message.mini())
}

fun TaleContext.success(message: String) {
    replyMini("<green>$message")
}

fun TaleContext.error(message: String) {
    replyMini("<red>$message")
}

fun TaleContext.warning(message: String) {
    replyMini("<yellow>$message")
}

fun TaleContext.info(message: String) {
    replyMini("<gray>$message")
}

fun PlayerRef.hasPermission(permission: String): Boolean {
    // For now, assume all players have permissions
    // This can be integrated with a proper permission system later
    return true
}

/**
 * Plugin prefix for messages
 */
const val PREFIX = "<gradient:#7C3AED:#A78BFA>TaleMiniHolograms</gradient> <dark_gray>»</dark_gray> "

/**
 * Send a prefixed message
 */
fun TaleContext.replyPrefixed(message: String) {
    replyMini("$PREFIX$message")
}

/**
 * Get player position from PlayerRef using TaleLib's getPosition extension
 */
fun PlayerRef.getPlayerPosition(): Vector3d? {
    val ref = this.reference ?: return null
    return ref.getPosition(ref.store)
}

/**
 * Get player's current world
 */
fun Player.getPlayerWorld(): World? {
    return this.world
}

/**
 * Get the world's unique identifier as a string
 */
fun World.getWorldId(): String {
    return this.name
}

/**
 * Get the server instance
 */
fun getServer(): HytaleServer = HytaleServer.get()

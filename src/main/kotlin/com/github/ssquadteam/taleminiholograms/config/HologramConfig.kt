package com.github.ssquadteam.taleminiholograms.config

import kotlinx.serialization.Serializable

/**
 * Plugin configuration stored in config.json
 */
@Serializable
data class HologramConfig(
    val defaultText: String = "Edit this Hologram using /tmh edit {name}",
    val lineSpacing: Double = 0.25,
    val autoSaveIntervalSeconds: Int = 300,
    val maxLinesPerHologram: Int = 0
)

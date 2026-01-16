package com.github.ssquadteam.taleminiholograms.data

import kotlinx.serialization.Serializable

/**
 * Data class representing a single hologram with its configuration
 */
@Serializable
data class HologramData(
    val name: String,
    val lines: List<String>,
    val worldUuid: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val createdBy: String,
    val createdAt: Long = System.currentTimeMillis(),
    val entityUuids: List<String> = emptyList()
) {

    fun withLines(newLines: List<String>): HologramData = copy(
        lines = newLines,
        entityUuids = emptyList()
    )

    fun withPosition(newX: Double, newY: Double, newZ: Double, newWorldUuid: String): HologramData =
        copy(x = newX, y = newY, z = newZ, worldUuid = newWorldUuid, entityUuids = emptyList())

    fun withName(newName: String): HologramData = copy(name = newName)

    fun clone(newName: String): HologramData = copy(
        name = newName,
        createdAt = System.currentTimeMillis(),
        entityUuids = emptyList() // Clear UUIDs for clone
    )

    fun withEntityUuids(uuids: List<String>): HologramData = copy(entityUuids = uuids)
}

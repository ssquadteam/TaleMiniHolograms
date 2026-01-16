package com.github.ssquadteam.taleminiholograms.config

import com.github.ssquadteam.taleminiholograms.data.HologramData
import kotlinx.serialization.Serializable

/**
 * Persistent storage for all holograms, stored in holograms.json
 */
@Serializable
data class HologramsStore(
    val holograms: Map<String, HologramData> = emptyMap()
) {

    fun add(hologram: HologramData): HologramsStore =
        copy(holograms = holograms + (hologram.name to hologram))

    fun remove(name: String): HologramsStore =
        copy(holograms = holograms - name)

    fun update(name: String, updater: (HologramData) -> HologramData): HologramsStore {
        val existing = holograms[name] ?: return this
        return copy(holograms = holograms + (name to updater(existing)))
    }

    fun get(name: String): HologramData? = holograms[name]

    fun exists(name: String): Boolean = holograms.containsKey(name)

    fun names(): Set<String> = holograms.keys

    fun count(): Int = holograms.size
}

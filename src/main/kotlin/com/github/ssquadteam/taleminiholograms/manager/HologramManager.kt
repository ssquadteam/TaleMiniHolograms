package com.github.ssquadteam.taleminiholograms.manager

import com.github.ssquadteam.talelib.config.ConfigManager
import com.github.ssquadteam.talelib.config.config
import com.github.ssquadteam.talelib.hologram.Hologram
import com.github.ssquadteam.talelib.hologram.hologram
import com.github.ssquadteam.talelib.hologram.HologramManager as TaleHologramManager
import com.github.ssquadteam.taleminiholograms.TaleMiniHolograms
import com.github.ssquadteam.taleminiholograms.config.HologramConfig
import com.github.ssquadteam.taleminiholograms.config.HologramsStore
import com.github.ssquadteam.taleminiholograms.data.HologramData
import com.github.ssquadteam.talelib.world.findWorldByName
import com.hypixel.hytale.component.RemoveReason
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Central manager for hologram lifecycle management.
 * Handles creation, deletion, updates, cloning, and renaming of holograms.
 */
class HologramManager(private val plugin: TaleMiniHolograms) {

    private val activeHolograms = ConcurrentHashMap<String, MutableList<Hologram>>()

    private val configManager: ConfigManager<HologramConfig> = plugin.config(
        default = HologramConfig(),
        fileName = "config.json"
    )

    private val storeManager: ConfigManager<HologramsStore> = plugin.config(
        default = HologramsStore(),
        fileName = "holograms.json"
    )

    val config: HologramConfig get() = configManager.config

    val store: HologramsStore get() = storeManager.config

    fun loadConfig() {
        configManager.load()
    }

    fun loadStore() {
        storeManager.load()
    }

    fun saveStore() {
        storeManager.save()
    }

    fun create(
        name: String,
        worldUuid: String,
        x: Double,
        y: Double,
        z: Double,
        creatorUuid: String
    ): Boolean {
        if (store.exists(name)) return false

        val defaultText = config.defaultText.replace("{name}", name)
        val data = HologramData(
            name = name,
            lines = listOf(defaultText),
            worldUuid = worldUuid,
            x = x,
            y = y,
            z = z,
            createdBy = creatorUuid
        )

        storeManager.save(store.add(data))
        spawnHologram(data)
        return true
    }

    fun delete(name: String): Boolean {
        if (!store.exists(name)) return false

        val data = store.get(name)
        despawnHologram(name)
        data?.let { removePersistedEntities(it) }
        storeManager.save(store.remove(name))
        return true
    }

    fun updateLines(name: String, newLines: List<String>) {
        val data = store.get(name) ?: return
        despawnHologram(name)
        removePersistedEntities(data)
        val updated = data.withLines(newLines)
        storeManager.save(store.update(name) { updated })
        spawnHologram(updated)
    }

    fun move(name: String, worldUuid: String, x: Double, y: Double, z: Double): Boolean {
        val data = store.get(name) ?: return false
        despawnHologram(name)
        removePersistedEntities(data)
        val updated = data.withPosition(x, y, z, worldUuid)
        storeManager.save(store.update(name) { updated })
        spawnHologram(updated)
        return true
    }

    private fun removePersistedEntities(data: HologramData) {
        if (data.entityUuids.isEmpty()) return

        val world = findWorldByName(data.worldUuid) ?: return

        world.execute {
            val entityStore = world.entityStore ?: return@execute
            val store = entityStore.store

            data.entityUuids.forEach { uuidStr ->
                try {
                    val uuid = UUID.fromString(uuidStr)
                    val entityRef = entityStore.getRefFromUUID(uuid)
                    if (entityRef != null && entityRef.isValid) {
                        store.removeEntity(entityRef, EntityStore.REGISTRY.newHolder(), RemoveReason.REMOVE)
                    }
                } catch (e: Exception) {
                    plugin.debug("Could not remove entity $uuidStr: ${e.message}")
                }
            }
        }
    }

    fun clone(sourceName: String, targetName: String? = null): String? {
        val sourceData = store.get(sourceName) ?: return null

        val newName = targetName ?: generateCloneName(sourceName)

        if (store.exists(newName)) return null

        val clonedData = sourceData.clone(newName)
        storeManager.save(store.add(clonedData))
        spawnHologram(clonedData)
        return newName
    }

    fun rename(oldName: String, newName: String): Boolean {
        if (!store.exists(oldName)) return false
        if (store.exists(newName)) return false

        val data = store.get(oldName) ?: return false
        val renamedData = data.withName(newName)

        despawnHologram(oldName)
        storeManager.save(store.remove(oldName).add(renamedData))
        spawnHologram(renamedData)
        return true
    }

    fun get(name: String): HologramData? = store.get(name)

    fun getAllNames(): Set<String> = store.names()

    fun spawnHologram(data: HologramData) {
        despawnHologram(data.name)

        val world = findWorldByName(data.worldUuid)
        if (world == null) {
            plugin.warn("Could not find world ${data.worldUuid} for hologram ${data.name}")
            return
        }

        val storedUuids = data.entityUuids
        val lineCount = data.lines.size

        if (storedUuids.isNotEmpty() && storedUuids.size == lineCount) {
            plugin.info("Hologram '${data.name}' tracked with ${storedUuids.size} persistent UUIDs")
            activeHolograms[data.name] = mutableListOf()
            return
        }

        world.execute {
            spawnFreshHologramEntities(data, world)
        }
    }

    private fun spawnFreshHologramEntities(data: HologramData, world: com.hypixel.hytale.server.core.universe.world.World) {
        val hologramEntities = mutableListOf<Hologram>()
        val newEntityUuids = mutableListOf<String>()
        val spacing = config.lineSpacing
        val lineCount = data.lines.size

        data.lines.forEachIndexed { index, line ->
            val lineY = data.y + (lineCount - 1 - index) * spacing

            val holograms = world.hologram {
                text(line)
                position(data.x, lineY, data.z)
            }

            holograms.firstOrNull()?.let { hologram ->
                hologramEntities.add(hologram)
                hologram.entityUuid?.let { uuid ->
                    newEntityUuids.add(uuid.toString())
                }
            }
        }

        if (hologramEntities.isNotEmpty()) {
            activeHolograms[data.name] = hologramEntities

            val storedUuids = data.entityUuids
            if (newEntityUuids != storedUuids && newEntityUuids.isNotEmpty()) {
                val updatedData = data.withEntityUuids(newEntityUuids)
                storeManager.save(store.update(data.name) { updatedData })
            }
        }
    }

    fun despawnHologram(name: String) {
        activeHolograms.remove(name)?.forEach { hologram ->
            hologram.remove()
        }
    }

    fun spawnAll() {
        store.holograms.values.forEach { spawnHologram(it) }
        plugin.info("Spawned ${store.count()} holograms")
    }

    fun despawnAll() {
        activeHolograms.keys.toList().forEach { despawnHologram(it) }
    }

    fun reload() {
        store.holograms.values.forEach { data ->
            removePersistedEntities(data)
        }

        despawnAll()

        val clearedStore = store.holograms.mapValues { (_, data) ->
            data.withEntityUuids(emptyList())
        }
        storeManager.save(HologramsStore(clearedStore))

        loadConfig()
        loadStore()

        spawnAll()
    }

    private fun generateCloneName(baseName: String): String {
        var suffix = 2
        var candidate = "$baseName$suffix"
        while (store.exists(candidate)) {
            suffix++
            candidate = "$baseName$suffix"
        }
        return candidate
    }
}

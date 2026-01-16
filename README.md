# TaleMiniHolograms

A comprehensive hologram plugin for Hytale servers, built with [TaleLib](https://github.com/ssquadteam/TaleLib).

## Features

- Create floating text holograms in your world
- Multi-line hologram support with configurable spacing
- Visual UI-based editor for editing hologram content
- Persistent holograms that survive server restarts
- Clone, rename, move, and delete holograms
- Auto-save functionality

## Commands

All commands use the `/tmh` prefix.

| Command | Permission | Description |
|---------|------------|-------------|
| `/tmh create <name>` | `tmh.create` | Create a new hologram at your location |
| `/tmh edit <name>` | `tmh.edit` | Open the visual UI editor |
| `/tmh delete <name>` | `tmh.delete` | Delete a hologram |
| `/tmh list` | `tmh.list` | List all holograms |
| `/tmh movehere <name>` | `tmh.move` | Move hologram to your location |
| `/tmh goto <name>` | `tmh.goto` | Teleport to a hologram |
| `/tmh clone <name> [newname]` | `tmh.clone` | Clone a hologram |
| `/tmh rename <old> <new>` | `tmh.rename` | Rename a hologram |
| `/tmh reload` | `tmh.admin` | Reload config and respawn holograms |
| `/tmh help` | - | Show help |

### Permission Groups

- `tmh.*` - All permissions
- `tmh.admin` - Admin commands (reload)

## UI Editor

Use `/tmh edit <name>` to open the visual hologram editor:

- **+ / -** buttons to add or remove lines
- **Text fields** for each line of the hologram
- **Scroll buttons** to navigate through lines (6 visible at a time)
- **Save / Cancel** buttons to apply or discard changes

## Configuration

### config.json

```json
{
  "defaultText": "Edit this Hologram using /tmh edit {name}",
  "lineSpacing": 0.25,
  "autoSaveIntervalSeconds": 300,
  "maxLinesPerHologram": 20
}
```

| Option | Description |
|--------|-------------|
| `defaultText` | Text shown when creating a new hologram. `{name}` is replaced with hologram name |
| `lineSpacing` | Vertical spacing between lines (in blocks) |
| `autoSaveIntervalSeconds` | Auto-save interval (0 to disable) |
| `maxLinesPerHologram` | Maximum lines per hologram |

### holograms.json

Stores all hologram data. Managed automatically - do not edit manually while server is running.

## Installation

1. Install [TaleLib](https://github.com/ssquadteam/TaleLib) plugin
2. Place `TaleMiniHolograms.jar` in your server's `plugins` folder
3. Start the server
4. Use `/tmh create test` to create your first hologram

---

# For Developers

TaleMiniHolograms is built on [TaleLib](https://github.com/ssquadteam/TaleLib), which provides a hologram API you can use in your own plugins.

## Using TaleLib for Holograms

Add TaleLib as a dependency:

```kotlin
// build.gradle.kts
dependencies {
    implementation(files("path/to/TaleLib.jar"))
}
```

### Basic Hologram Creation

```kotlin
import com.github.ssquadteam.talelib.hologram.*

// Single line hologram
val hologram = world.createHologram("Hello World!", x, y, z)

// Multi-line hologram
val holograms = world.createMultiLineHologram(
    listOf("Line 1", "Line 2", "Line 3"),
    x, y, z,
    lineSpacing = 0.25
)

// DSL builder
val holos = world.hologram {
    line("Welcome!")
    line("To the server")
    position(x, y, z)
    spacing(0.3)
}

// Manipulate hologram
hologram?.text = "Updated text"
hologram?.setPosition(newX, newY, newZ)
hologram?.remove()
```

## Entity Persistence - Critical Knowledge

Hytale automatically persists entities with `UUIDComponent` to the world. This means hologram entities **survive server restarts**.

### The Problem

If you naively spawn holograms on startup, you'll create duplicates:

```kotlin
// WRONG - causes duplicates on every restart!
override fun onStart() {
    myHolograms.forEach { data ->
        world.createHologram(data.text, data.x, data.y, data.z)
    }
}
```

### The Solution - UUID-Based Tracking

Store entity UUIDs when spawning, and on restart, **trust that persisted entities exist**:

```kotlin
import kotlinx.serialization.Serializable

@Serializable
data class MyHologramData(
    val name: String,
    val lines: List<String>,
    val worldName: String,
    val x: Double,
    val y: Double,
    val z: Double,
    val entityUuids: List<String> = emptyList()  // Track spawned entity UUIDs
)

fun spawnHologram(data: MyHologramData) {
    val world = findWorldByName(data.worldName) ?: return

    // If UUIDs exist and match line count - DON'T spawn!
    // Entities are already persisted in the world
    if (data.entityUuids.isNotEmpty() && data.entityUuids.size == data.lines.size) {
        logger.info("Hologram '${data.name}' using ${data.entityUuids.size} persisted entities")
        return
    }

    // No UUIDs or count mismatch - spawn fresh entities
    world.execute {
        val newUuids = mutableListOf<String>()

        data.lines.forEachIndexed { index, line ->
            val lineY = data.y + (data.lines.size - 1 - index) * 0.25

            val holograms = world.hologram {
                text(line)
                position(data.x, lineY, data.z)
            }

            holograms.firstOrNull()?.entityUuid?.let { uuid ->
                newUuids.add(uuid.toString())
            }
        }

        // Save UUIDs for next restart
        if (newUuids.isNotEmpty()) {
            saveHologramUuids(data.name, newUuids)
        }
    }
}
```

### Updating Hologram Content

When content changes, you must:
1. Remove the old persisted entities from the world
2. Clear the stored UUIDs
3. Spawn new entities

```kotlin
import com.hypixel.hytale.component.RemoveReason
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore

fun updateHologramLines(name: String, newLines: List<String>) {
    val data = getHologramData(name) ?: return
    val world = findWorldByName(data.worldName) ?: return

    // Remove old persisted entities from world
    if (data.entityUuids.isNotEmpty()) {
        world.execute {
            val entityStore = world.entityStore ?: return@execute
            val store = entityStore.store

            data.entityUuids.forEach { uuidStr ->
                try {
                    val uuid = UUID.fromString(uuidStr)
                    val entityRef = entityStore.getRefFromUUID(uuid)
                    if (entityRef != null && entityRef.isValid) {
                        store.removeEntity(
                            entityRef,
                            EntityStore.REGISTRY.newHolder(),
                            RemoveReason.REMOVE
                        )
                    }
                } catch (e: Exception) {
                    // Entity may already be removed
                }
            }
        }
    }

    // Save with cleared UUIDs to force respawn
    val updated = data.copy(
        lines = newLines,
        entityUuids = emptyList()
    )
    saveHologramData(updated)

    // Spawn with new content
    spawnHologram(updated)
}
```

### Deleting Holograms

Always remove persisted entities when deleting:

```kotlin
fun deleteHologram(name: String) {
    val data = getHologramData(name) ?: return
    val world = findWorldByName(data.worldName) ?: return

    // Remove persisted entities
    if (data.entityUuids.isNotEmpty()) {
        world.execute {
            val entityStore = world.entityStore ?: return@execute
            val store = entityStore.store

            data.entityUuids.forEach { uuidStr ->
                try {
                    val uuid = UUID.fromString(uuidStr)
                    val entityRef = entityStore.getRefFromUUID(uuid)
                    if (entityRef != null && entityRef.isValid) {
                        store.removeEntity(
                            entityRef,
                            EntityStore.REGISTRY.newHolder(),
                            RemoveReason.REMOVE
                        )
                    }
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    // Remove from storage
    removeHologramData(name)
}
```

### Quick Reference

| Scenario | Action |
|----------|--------|
| Server startup with stored UUIDs | DON'T spawn - trust persistence |
| Server startup with no UUIDs | Spawn new entities, save UUIDs |
| Content changed (lines edited) | Remove old entities, clear UUIDs, spawn new |
| Hologram deleted | Remove entities from world by UUID |
| Hologram cloned | Clone with empty UUIDs (forces new spawn) |
| Position changed | Remove old entities, clear UUIDs, spawn at new position |

### Important Notes

1. **Nameplate Limitations**: Hytale's `Nameplate` component only supports plain text. MiniMessage tags and colors will display as raw text.

2. **WorldThread Requirement**: Entity operations must run on the WorldThread. Use `world.execute { }` for spawning and removing entities.

3. **TaleLib's entityUuid**: The `Hologram` class provides `entityUuid` which is the UUID from `UUIDComponent`. This is what Hytale uses for persistence.

## Dependencies

- [TaleLib](https://github.com/ssquadteam/TaleLib) - Required

## License

MIT License - See LICENSE file for details.

## Credits

- Built with [TaleLib](https://github.com/ssquadteam/TaleLib)

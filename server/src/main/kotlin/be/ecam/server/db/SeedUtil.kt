package be.ecam.server.db

import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.io.File

// Try classpath first, then filesystem path -- put resources under server/src/main/resources/data/
fun loadResourceText(resourcePath: String): String? {
    val normalized = resourcePath.removePrefix("/")
    Thread.currentThread().contextClassLoader.getResourceAsStream(normalized)?.use { s ->
        return s.bufferedReader().use { it.readText() }
    }
    val f = File(resourcePath)
    return if (f.exists()) f.readText() else null
}

inline fun <reified T> decodeJsonList(json: String): List<T> =
    Json { ignoreUnknownKeys = true }.decodeFromString(json)

/**
 * Generic seeder helper that reads JSON and applies create() for items that don't exist.
 * - resourcePath: classpath path under resources (e.g. "data/admin.json")
 * - exists: fast predicate to check if DTO already exists (avoid duplication)
 * - create: create action (should use service.create(...) which wraps transaction)
 */
inline fun <reified T> seedFromResourceIfMissing(
    resourcePath: String,
    noinline decode: (String) -> List<T> = { decodeJsonList<T>(it) },
    noinline exists: (T) -> Boolean,
    noinline create: (T) -> Unit
) {
    val text = loadResourceText(resourcePath)
    if (text.isNullOrBlank()) {
        println("Seed file not found: $resourcePath")
        return
    }

    val items = try {
        decode(text)
    } catch (ex: Exception) {
        println("Failed to decode $resourcePath: ${ex.message}")
        return
    }

    println("Seeding ${items.size} items from $resourcePath")
    items.forEach { dto ->
        try {
            if (!exists(dto)) {
                create(dto)
            } else {
                // keep logs minimal
                // println("Skipping existing: ${dto}")
            }
        } catch (e: Exception) {
            println("Failed to create item from $resourcePath: ${e.message}")
        }
    }
}
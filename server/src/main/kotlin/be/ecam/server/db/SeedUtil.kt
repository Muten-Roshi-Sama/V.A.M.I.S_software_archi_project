package be.ecam.server.db

import kotlinx.serialization.json.*
import kotlinx.serialization.*
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

// Check if all instances within JSON have been successfullly added
data class SeedResult(val name: String, val inserted: Int = 0, val skipped: Int = 0, val errors: List<String> = emptyList())

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
    name:String,
    noinline decode: (String) -> List<T> = { decodeJsonList<T>(it) },
    noinline exists: (T) -> Boolean,
    noinline create: (T) -> Unit,
    noinline legacyMapper: (Map<String, Any?>) -> T? = { null } // optional: map legacy JSON object

): SeedResult {
    val text = loadResourceText(resourcePath)
    if (text.isNullOrBlank()) {
        println("Seed file not found: $resourcePath")
        return SeedResult(name, 0, 0, listOf("file-not-found"))
    }

    val items: List<T> = try {
        decode(text)
    } catch (ex: Exception) {
        // Fallback: try to decode as an array of simple maps (string -> string) and run legacyMapper
        try {
            val rawList: List<Map<String, String>> = Json { ignoreUnknownKeys = true }.decodeFromString(text)
            val mapped = rawList.mapNotNull { map ->
                // Map<String,String> -> Map<String,Any?> matches legacyMapper input
                legacyMapper(map as Map<String, Any?>) as T?
            }
            if (mapped.isEmpty()) throw ex
            mapped
        } catch (ex2: Exception) {
            println("Failed to decode $resourcePath: ${ex.message}")
            return SeedResult(name, 0, 0, listOf("decode-failed:${ex.message}"))
        }
    }

    var inserted = 0
    var skipped = 0
    val errors = mutableListOf<String>()
    items.forEachIndexed { i, dto ->
        try {
            if (!exists(dto)) {
                create(dto)
                inserted++
            } else skipped++
        } catch (e: Exception) {
            val msg = "item[$i] error: ${e.message}"
            println(msg)
            errors += msg
        }
    }
    println("Seeding '$name' complete: inserted=$inserted skipped=$skipped errors=${errors.size}")
    return SeedResult(name, inserted, skipped, errors)




//    println("Seeding ${items.size} items from $resourcePath")
//    items.forEach { dto ->
//        try {
//            if (!exists(dto)) {
//                create(dto)
//            } else {
//                // keep logs minimal
//                // println("Skipping existing: ${dto}")
//            }
//        } catch (e: Exception) {
//            println("Failed to create item from $resourcePath: ${e.message}")
//        }
//    }
//}

}
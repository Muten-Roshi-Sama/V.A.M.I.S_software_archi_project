package be.ecam.server

import be.ecam.common.api.*

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer
import kotlin.test.Test
import kotlin.test.fail

/**
 * Validate seed JSON files by decoding each element into DTOs (kotlinx.serialization).
 * Mapping file: src/main/resources/data/_mapping.json
 * Example mapping:
 * {
 *   "admin.json": "be.ecam.common.api.AdminDTO",
 *   "student.json": ["be.ecam.common.api.PersonDTO", "be.ecam.common.api.StudentDTO"]
 * }
 *
 * Add DTO serializers to `dtoRegistry` list below as you create DTOs.
 */
class SeedDtoValidationTest {

    companion object {
        private const val MAPPING_RESOURCE = "data/_mapping.json"
        private val json = Json { ignoreUnknownKeys = true }

        // Programmatic registry built from serializer instances (single place to add DTOs)
        private val dtoRegistry: Map<String, KSerializer<*>> = listOf(
            AdminDTO.serializer()
            // Add future DTO serializers here:
            // , StudentDTO.serializer()
            // , PersonDTO.serializer()
        ).associateBy { it.descriptor.serialName }

        private fun loadMapping(): JsonObject {
            val url = Thread.currentThread().contextClassLoader.getResource(MAPPING_RESOURCE)
                ?: throw IllegalArgumentException("Mapping file not found on classpath at $MAPPING_RESOURCE")
            val content = url.openStream().bufferedReader().use { it.readText() }
            val el = try {
                json.parseToJsonElement(content)
            } catch (ex: Exception) {
                throw IllegalArgumentException("Failed to parse mapping JSON: ${ex.message}", ex)
            }
            if (el !is JsonObject) throw IllegalArgumentException("Mapping file must be a JSON object mapping filename -> DTO or [DTOs]")
            return el
        }

        private fun loadSeedArray(filename: String): JsonArray {
            val resource = "data/$filename"
            val url = Thread.currentThread().contextClassLoader.getResource(resource)
                ?: throw IllegalArgumentException("Seed file not found on classpath at $resource")
            val content = url.openStream().bufferedReader().use { it.readText() }
            val el = try {
                json.parseToJsonElement(content)
            } catch (ex: Exception) {
                throw IllegalArgumentException("Failed to parse JSON in $filename: ${ex.message}", ex)
            }
            if (el !is JsonArray) throw IllegalArgumentException("Seed file $filename must contain a top-level JSON array")
            return el
        }

        // Get set of expected top-level fields from serializer's descriptor
        private fun expectedFieldsFromSerializer(serializer: KSerializer<*>): Set<String> {
            val d = serializer.descriptor
            val names = mutableSetOf<String>()
            for (i in 0 until d.elementsCount) names.add(d.getElementName(i))
            return names
        }

        @Suppress("UNCHECKED_CAST")
        private fun validateElementAgainstDto(
            filename: String,
            idx: Int,
            elem: JsonObject,
            serializer: KSerializer<*>,
            strictMode: Boolean,
            errors: MutableList<String>
        ) {
            // Direct decode (assumes camelCase keys that match DTO names)
            try {
                json.decodeFromJsonElement(serializer as KSerializer<Any>, elem)
            } catch (ex: Exception) {
                errors += "$filename[$idx]: DTO decode error -> ${ex.message}"
            }

            if (strictMode) {
                val expected = expectedFieldsFromSerializer(serializer)
                val present = elem.keys
                val unknown = present - expected
                if (unknown.isNotEmpty()) {
                    errors += "$filename[$idx]: unknown key(s) for DTO ${serializer.descriptor.serialName}: ${unknown.sorted()}"
                }
                // report missing required keys (non-optional in descriptor)
                val missing = mutableListOf<String>()
                val d = serializer.descriptor
                for (i in 0 until d.elementsCount) {
                    if (!d.isElementOptional(i)) {
                        val name = d.getElementName(i)
                        if (name !in present) missing += name
                    }
                }
                if (missing.isNotEmpty()) {
                    errors += "$filename[$idx]: missing required key(s) for DTO ${serializer.descriptor.serialName}: ${missing.sorted()}"
                }
            }
        }
    }

    @Test
    fun `validate seed jsons against DTOs`() {
        val strictMode = System.getProperty("seed.strict", "false").toBoolean()
        val mapping = try {
            loadMapping()
        } catch (ex: Exception) {
            fail("Could not load mapping: ${ex.message}")
        }

        val errors = mutableListOf<String>()

        for ((fileName, mapped) in mapping) {
            // mapping value may be string or array
            val dtoNames: List<String> = when (mapped) {
                is JsonPrimitive -> listOf(mapped.content)
                is JsonArray -> mapped.mapNotNull { it.jsonPrimitive.contentOrNull }
                else -> {
                    errors += "$fileName: invalid mapping value type (must be string or array)"
                    continue
                }
            }

            val seedArray = try {
                loadSeedArray(fileName)
            } catch (ex: Exception) {
                errors += "$fileName: ${ex.message}"
                continue
            }

            seedArray.forEachIndexed { idx, je ->
                if (je !is JsonObject) {
                    errors += "$fileName[$idx]: element is not an object"
                    return@forEachIndexed
                }
                for (dtoName in dtoNames) {
                    val serializer = dtoRegistry[dtoName]
                    if (serializer == null) {
                        errors += "$fileName[$idx]: DTO serializer not registered for '$dtoName' (add serializer to dtoRegistry in test)"
                        continue
                    }
                    validateElementAgainstDto(fileName, idx, je, serializer, strictMode, errors)
                }
            }
        }

        if (errors.isNotEmpty()) {
            fail("Seed validation failures:\n" + errors.joinToString("\n"))
        }
    }
}
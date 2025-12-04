package be.ecam.server.services

import be.ecam.server.models.TeacherTable
import be.ecam.server.models.ModulesTable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import be.ecam.common.api.Teacher
import be.ecam.common.api.Module

@Serializable
data class TeacherSeedDTO(
    val teacher_id: Int,
    val email: String,
    val first_name: String,
    val last_name: String,
    val password: String? = null,
    val created_at: String
)

@Serializable
data class ModuleSeedDTO(
    val activity_name: String,
    val activity_code: String,
    val ects: Int,
    val description: String,
    val coordinator: String,
    val course_code: String? = null
)

class TeacherService {
    private val json = Json { ignoreUnknownKeys = true }

    fun seedFromJson() {
        val possiblePaths = listOf(
            "server/src/main/resources/data/teachers.json",
            "src/main/resources/data/teachers.json",
            "data/teachers.json"
        )
        val file = possiblePaths.map(::File).firstOrNull { it.exists() }
            ?: run {
                println("Warning: teachers.json not found in any expected path")
                return
            }

        val teachers = json.decodeFromString<List<TeacherSeedDTO>>(file.readText())

        transaction {
            if (TeacherTable.selectAll().count() > 0L) {
                println("Info: Teachers already seeded, skipping")
                return@transaction
            }

            teachers.forEach { teacherDto ->
                TeacherTable.insert { row ->
                    row[TeacherTable.teacherId] = teacherDto.teacher_id
                    row[TeacherTable.email] = teacherDto.email
                    row[TeacherTable.firstName] = teacherDto.first_name
                    row[TeacherTable.lastName] = teacherDto.last_name
                    row[TeacherTable.password] = teacherDto.password
                    row[TeacherTable.createdAt] = teacherDto.created_at
                }
            }

            println("Success: ${teachers.size} teachers seeded from teachers.json")
        }
    }

    fun seedModulesFromJson() {
        val possiblePaths = listOf(
            "server/src/main/resources/data/modules.json",
            "src/main/resources/data/modules.json",
            "data/modules.json"
        )
        val file = possiblePaths.map(::File).firstOrNull { it.exists() }
            ?: run {
                println("Warning: modules.json not found in any expected path")
                return
            }

        val modules = json.decodeFromString<List<ModuleSeedDTO>>(file.readText())

        transaction {
            if (ModulesTable.selectAll().count() > 0L) {
                println("Info: Modules already seeded, skipping")
                return@transaction
            }

            modules.forEach { moduleDto ->
                ModulesTable.insert { row ->
                    row[ModulesTable.activityName] = moduleDto.activity_name
                    row[ModulesTable.activityCode] = moduleDto.activity_code
                    row[ModulesTable.ects] = moduleDto.ects
                    row[ModulesTable.description] = moduleDto.description
                    row[ModulesTable.coordinator] = moduleDto.coordinator
                    row[ModulesTable.courseCode] = moduleDto.course_code
                }
            }

            println("Success: ${modules.size} modules seeded from modules.json")
        }
    }

    fun getAllTeachers(): List<Teacher> {
        return transaction {
            TeacherTable.selectAll().map { tRow ->
                val email = tRow[TeacherTable.email]
                val first = tRow[TeacherTable.firstName]
                val last = tRow[TeacherTable.lastName]

                // ✅ CORRECTION : Syntaxe correcte pour Exposed
                val modules = ModulesTable
                    .selectAll()
                    .where { ModulesTable.coordinator eq email }
                    .map { mRow ->
                        Module(
                            activity_name = mRow[ModulesTable.activityName],
                            description = mRow[ModulesTable.description] ?: "",
                            activity_code = mRow[ModulesTable.activityCode],
                            ects = mRow[ModulesTable.ects]
                        )
                    }

                Teacher(
                    email = email,
                    first_name = first,
                    last_name = last,
                    modules = modules
                )
            }
        }
    }

    fun getTeacherByEmail(emailParam: String): Teacher? {
        return transaction {
            // ✅ CORRECTION : Syntaxe correcte pour Exposed
            val tRow = TeacherTable
                .selectAll()
                .where { TeacherTable.email eq emailParam }
                .firstOrNull() ?: return@transaction null

            // ✅ CORRECTION : Syntaxe correcte pour Exposed
            val modules = ModulesTable
                .selectAll()
                .where { ModulesTable.coordinator eq emailParam }
                .map { mRow ->
                    Module(
                        activity_name = mRow[ModulesTable.activityName],
                        description = mRow[ModulesTable.description] ?: "",
                        activity_code = mRow[ModulesTable.activityCode],
                        ects = mRow[ModulesTable.ects]
                    )
                }

            Teacher(
                email = tRow[TeacherTable.email],
                first_name = tRow[TeacherTable.firstName],
                last_name = tRow[TeacherTable.lastName],
                modules = modules
            )
        }
    }

    fun findTeachersByName(q: String): List<Teacher> {
        val needle = q.trim().lowercase()
        if (needle.isEmpty()) return emptyList()

        return getAllTeachers().filter { t ->
            t.first_name.lowercase().contains(needle) ||
                    t.last_name.lowercase().contains(needle)
        }
    }
}
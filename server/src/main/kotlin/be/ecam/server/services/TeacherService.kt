package be.ecam.server.services

import be.ecam.server.models.TeacherTable
import be.ecam.server.models.ModulesTable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import be.ecam.common.api.Teacher
import be.ecam.common.api.Module

//DTOs used only to read JSON at startup (seed)
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
    //Seed function called once when the server starts up
    //It reads students.json and fills the database
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

    //// ========== CRUD : READ from BD ==========
    fun getAllTeachers(): List<Teacher> {
        return transaction {
            // SELECT * FROM teachers
            TeacherTable.selectAll().map { tRow ->
                val email = tRow[TeacherTable.email]
                val first = tRow[TeacherTable.firstName]
                val last = tRow[TeacherTable.lastName]

                //retrieval modules
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

    //Getting one student by his email
    fun getTeacherByEmail(emailParam: String): Teacher? {
        return transaction {
            val tRow = TeacherTable
                .selectAll()
                .where { TeacherTable.email eq emailParam }
                .firstOrNull() ?: return@transaction null

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

    //Filter
    fun findTeachersByName(q: String): List<Teacher> {
        val needle = q.trim().lowercase()
        if (needle.isEmpty()) return emptyList()

        return getAllTeachers().filter { t ->
            t.first_name.lowercase().contains(needle) ||
                    t.last_name.lowercase().contains(needle)
        }
    }

    //// ========== CREATE (change in the database) ==========
    fun createTeacher(teacher: TeacherSeedDTO) {
        transaction {
            // Vérifie si le teacher existe déjà
            val existing = TeacherTable
                .selectAll()
                .where {
                    (TeacherTable.email eq teacher.email) or
                            (TeacherTable.teacherId eq teacher.teacher_id)
                }
                .count()

            if (existing > 0L) {
                throw IllegalArgumentException("Teacher with this email or teacherId already exists")
            }

            //insert into teachers
            TeacherTable.insert { row ->
                row[teacherId] = teacher.teacher_id
                row[email] = teacher.email
                row[firstName] = teacher.first_name
                row[lastName] = teacher.last_name
                row[password] = teacher.password
                row[createdAt] = teacher.created_at
            }

            println("Teacher created: ${teacher.email}")
        }
    }

    //Adding a module
    fun addModule(module: ModuleSeedDTO) {
        transaction {
            val teacherExists = TeacherTable
                .selectAll()
                .where { TeacherTable.email eq module.coordinator }
                .count()
            if (teacherExists == 0L) {
                throw IllegalArgumentException("Teacher (coordinator) not found: ${module.coordinator}")
            }
            val moduleExists = ModulesTable
                .selectAll()
                .where { ModulesTable.activityCode eq module.activity_code }
                .count()

            if (moduleExists > 0L) {
                throw IllegalArgumentException("Module with this activity_code already exists: ${module.activity_code}")
            }

            //insert into modules
            ModulesTable.insert { row ->
                row[activityName] = module.activity_name
                row[activityCode] = module.activity_code
                row[ects] = module.ects
                row[description] = module.description
                row[coordinator] = module.coordinator
                row[courseCode] = module.course_code
            }

            println("Module added: ${module.activity_code} for ${module.coordinator}")
        }
    }

    // ========== UPDATE change in the database ==========

    fun updateTeacher(emailParam: String, newData: TeacherSeedDTO) {
        transaction {
            // UPDATE teachers SET ... WHERE email = ?
            val updated = TeacherTable.update({ TeacherTable.email eq emailParam }) { row ->
                row[firstName] = newData.first_name
                row[lastName] = newData.last_name
                row[password] = newData.password
            }

            if (updated == 0) {
                throw IllegalArgumentException("Teacher not found: $emailParam")
            }

            println("Teacher updated: $emailParam")
        }
    }

    fun updateModule(activityCode: String, newData: ModuleSeedDTO) {
        transaction {
            val updated = ModulesTable.update({ ModulesTable.activityCode eq activityCode }) { row ->
                row[activityName] = newData.activity_name
                row[ects] = newData.ects
                row[description] = newData.description
                row[coordinator] = newData.coordinator
                row[courseCode] = newData.course_code
            }

            if (updated == 0) {
                throw IllegalArgumentException("Module not found: $activityCode")
            }

            println("Module updated: $activityCode")
        }
    }

    // ========== DELETE ==========

    //Delete the teacher with his modules
    fun deleteTeacher(emailParam: String) {
        transaction {
            val teacherExists = TeacherTable
                .selectAll()
                .where { TeacherTable.email eq emailParam }
                .count()

            if (teacherExists == 0L) {
                throw IllegalArgumentException("Teacher not found: $emailParam")
            }

            val deletedModules = ModulesTable.deleteWhere {
                ModulesTable.coordinator eq emailParam
            }

            val deletedTeacher = TeacherTable.deleteWhere {
                TeacherTable.email eq emailParam
            }

            println("Teacher deleted: $emailParam ($deletedModules modules removed)")
        }
    }

    fun deleteModule(activityCode: String) {
        transaction {
            val deleted = ModulesTable.deleteWhere {
                ModulesTable.activityCode eq activityCode
            }

            if (deleted == 0) {
                throw IllegalArgumentException("Module not found: $activityCode")
            }

            println("Module deleted: $activityCode")
        }
    }
}
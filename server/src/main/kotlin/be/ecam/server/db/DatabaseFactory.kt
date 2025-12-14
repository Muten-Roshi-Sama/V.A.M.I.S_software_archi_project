package be.ecam.server.db

import be.ecam.server.models.AdminTable
import be.ecam.server.models.EvaluationTable
import be.ecam.server.models.StudentTable
import be.ecam.server.models.TeacherTable
import be.ecam.server.models.ModulesTable
import be.ecam.common.api.AdminDTO
import be.ecam.server.models.Admin
import be.ecam.server.models.OptionTable
import be.ecam.server.models.CourseTable
import be.ecam.server.models.AnnualStudyPlanTable
import be.ecam.server.models.PlanCourseTable
import be.ecam.server.services.AdminService
import be.ecam.server.services.StudentService
import be.ecam.server.services.TeacherService
import be.ecam.server.services.BibleService
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.time.Year
import java.io.File

object DatabaseFactory {

    private var dbPath: String? = null

    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class AdminSeedDTO(
        val id: Int? = null,
        val first_name: String? = null,
        val last_name: String? = null,
        val email: String,
        val password: String? = null,
        val created_at: String? = null,
    )

    fun connect() {
        val dbFolder = File("data")
        if (!dbFolder.exists()) {
            dbFolder.mkdirs()
            println("Created data directory")
        }

        val dbFile = File(dbFolder, "sqlite.db")
        dbPath = dbFile.absolutePath
        Database.connect("jdbc:sqlite:${dbFile.absolutePath}", driver = "org.sqlite.JDBC")
        println("Connected to SQLite database, path is : ${dbFile.absolutePath}")
    }

    fun initDb() {
        initDbInternal(allowReset = true)
    }

    private fun initDbInternal(allowReset: Boolean) {
        try {
            ensureSchema()
            initAdmins()
            seedStudentsIfEmpty()
            seedGradesIfEmpty()
            migrateEvaluationSessionsIfNeeded()
            seedTeachersIfEmpty()
            seedModulesIfEmpty()
            seedOptionsIfEmpty()
            seedCoursesIfEmpty()
            seedStudyPlansIfEmpty()
        } catch (e: Exception) {
            if (allowReset && looksLikeSchemaMismatch(e)) {
                println("Detected incompatible SQLite schema. Resetting sqlite.db and retrying...")
                resetDbFile()
                connect()
                initDbInternal(allowReset = false)
                return
            }
            throw e
        }
    }

    private fun ensureSchema() {
        transaction {
            try {
                SchemaUtils.create(AdminTable)
            } catch (_: Exception) {
                // Table may already exist
            }

            try {
                SchemaUtils.create(StudentTable, EvaluationTable)
            } catch (_: Exception) {
                // Tables may already exist
            }

            try {
                SchemaUtils.create(TeacherTable, ModulesTable)
            } catch (_: Exception) {
                // Tables may already exist
            }

            try {
                SchemaUtils.create(OptionTable, CourseTable, AnnualStudyPlanTable, PlanCourseTable)
            } catch (_: Exception) {
                // Tables may already exist
            }
        }

        try {
            transaction {
                // Force SQLite to validate column existence/types by selecting all columns.
                AdminTable.selectAll().limit(1).toList()
                StudentTable.selectAll().limit(1).toList()
                EvaluationTable.selectAll().limit(1).toList()
                TeacherTable.selectAll().limit(1).toList()
                ModulesTable.selectAll().limit(1).toList()
                OptionTable.selectAll().limit(1).toList()
                CourseTable.selectAll().limit(1).toList()
                AnnualStudyPlanTable.selectAll().limit(1).toList()
                PlanCourseTable.selectAll().limit(1).toList()
            }
        } catch (e: Exception) {
            throw IllegalStateException("Database schema mismatch", e)
        }
    }

    private fun looksLikeSchemaMismatch(error: Throwable): Boolean {
        val markers = listOf(
            "no such column",
            "has no column",
            "cannot add a not null column",
            "duplicate column name",
            "query returns results",
            "database schema mismatch",
        )

        var current: Throwable? = error
        while (current != null) {
            val message = (current.message ?: "").lowercase()
            if (markers.any { message.contains(it) }) return true
            current = current.cause
        }
        return false
    }

    private fun resetDbFile() {
        val path = dbPath ?: return
        val dbFile = File(path)
        if (!dbFile.exists()) return

        val backup = File(dbFile.parentFile, "sqlite.db.bak-${System.currentTimeMillis()}")
        val renamed = dbFile.renameTo(backup)
        if (renamed) {
            println("Backed up incompatible DB to: ${backup.absolutePath}")
            return
        }

        val deleted = dbFile.delete()
        if (deleted) {
            println("Deleted incompatible DB at: ${dbFile.absolutePath}")
        } else {
            println("Failed to reset sqlite.db (rename & delete failed). Please remove it manually: ${dbFile.absolutePath}")
        }
    }

    private fun seedStudentsIfEmpty() {
        val count = transaction {
            StudentTable.selectAll().count()
        }

        if (count == 0L) {
            println("No students found → seeding from students.json")
            StudentService().seedFromJson()
        } else {
            println("Student table already contains $count student(s). Skipping seed.")
        }
    }

    private fun seedGradesIfEmpty() {
        val count = transaction { EvaluationTable.selectAll().count() }
        println("Syncing grades from grades.json (existing evaluations: $count)")
        StudentService().syncGradesFromJson()
    }

    private fun migrateEvaluationSessionsIfNeeded() {
        val desired = Year.now().value.toString()
        val updated = transaction {
            EvaluationTable.update({ EvaluationTable.session eq "N/A" }) { row ->
                row[EvaluationTable.session] = desired
            }
        }
        if (updated > 0) {
            println("Updated $updated evaluation(s) session from N/A to $desired")
        }
    }

    private fun initAdmins() {
        val possiblePaths = listOf(
            "server/src/main/resources/data/admin.json",
            "src/main/resources/data/admin.json",
            "data/admin.json"
        )

        val adminFile = possiblePaths.map { File(it) }.firstOrNull { it.exists() }

        if (adminFile == null) {
            val resourceStream = this::class.java.classLoader.getResourceAsStream("data/admin.json")
            if (resourceStream != null) {
                val jsonString = resourceStream.bufferedReader().use { it.readText() }
                processAdminData(jsonString)
                return
            }
            println("No mock admin data file found.")
            return
        }

        println("Loading admin data from: ${adminFile.absolutePath}")
        val jsonString = adminFile.readText()
        processAdminData(jsonString)
    }

    private fun processAdminData(jsonString: String) {
        val service = AdminService()
        val existing = service.getAll()

        if (existing.isEmpty()) {
            val seeds = json.decodeFromString<List<AdminSeedDTO>>(jsonString)
            val adminDTOs = seeds.map { seed ->
                AdminDTO(
                    username = seed.email.substringBefore('@'),
                    email = seed.email,
                    password = seed.password,
                )
            }
            adminDTOs.forEach { service.create(it) }
            println("Inserted ${adminDTOs.size} mock admins from JSON.")

            transaction {
                println("=== Current Admins in DB ===")
                Admin.all().forEach { println("ID=${it.id.value} | username=${it.username} | email=${it.email}") }
                println("============================")
            }
        } else {
            println("Admin table already contains ${existing.size} admins.")
        }
    }
    private fun seedTeachersIfEmpty() {
        val count = transaction { TeacherTable.selectAll().count() }
        if (count == 0L) {
            println("No teachers found → seeding from teachers.json")
            TeacherService().seedFromJson()
        } else {
            println("Teacher table already contains $count teacher(s). Skipping seed.")
        }
    }

    private fun seedModulesIfEmpty() {
        val count = transaction { ModulesTable.selectAll().count() }
        if (count == 0L) {
            println("No modules found → seeding from modules.json")
            TeacherService().seedModulesFromJson()
        } else {
            println("Modules table already contains $count module(s). Skipping seed.")
        }
    }
    private fun seedOptionsIfEmpty() {
        val count = transaction { OptionTable.selectAll().count() }
        if (count == 0L) {
            println("No options found → seeding from options.json")
            BibleService().seedOptions()
        } else {
            println("Options table already contains $count option(s). Skipping seed.")
        }
    }
    private fun seedCoursesIfEmpty() {
        val count = transaction { CourseTable.selectAll().count() }
        if (count == 0L) {
            println("No courses found → seeding from courses.json")
            BibleService().seedCourses()
        } else {
            println("Courses table already contains $count course(s). Skipping seed.")
        }
    }
    private fun seedStudyPlansIfEmpty() {
        val count = transaction { AnnualStudyPlanTable.selectAll().count() }
        if (count == 0L) {
            println("No study plans found → seeding from annual_study_plans.json")
            BibleService().seedAnnualStudyPlans()
        } else {
            println("Study plans table already contains $count plan(s). Skipping seed.")
        }
    }
}
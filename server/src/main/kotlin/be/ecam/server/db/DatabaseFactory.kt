package be.ecam.server.db

// DAO Schema
import be.ecam.server.models.PersonTable
import be.ecam.server.models.AdminTable
import be.ecam.server.models.EvaluationTable
import be.ecam.server.models.StudentTable
import be.ecam.server.models.TeacherTable
import be.ecam.server.models.ModulesTable
import be.ecam.server.models.OptionTable
import be.ecam.server.models.CourseTable
import be.ecam.server.models.AnnualStudyPlanTable
import be.ecam.server.models.PlanCourseTable
import be.ecam.server.models.ScheduleTable

// DAO Class
import be.ecam.server.models.Admin
import be.ecam.server.models.Teacher
import be.ecam.server.models.Student
//import be.ecam.server.models.Course

// DAO Services
import be.ecam.server.services.AdminService
import be.ecam.server.services.StudentService
import be.ecam.server.services.TeacherService
import be.ecam.server.services.BibleService
import be.ecam.server.services.ScheduleService

// Shared DTO
import be.ecam.common.api.AdminDTO
import be.ecam.common.api.TeacherDTO
import be.ecam.common.api.StudentDTO
//import be.ecam.common.api.CourseDTO


// Kotlin imports
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.selectAll
import java.io.File
import java.sql.Statement

// ====================================================================
object DatabaseFactory {
    private var dbPath: String? = null
    private var database: Database? = null

    fun connect() {
        val dbFolder = File("data")
        if (!dbFolder.exists()) {
            dbFolder.mkdirs()
            println("Created data directory")
        }

        // Get DB file path
        val path = File(dbFolder, "sqlite.db").absolutePath
        dbPath = path

        //Database.connect("jdbc:sqlite:$dbPath", driver = "org.sqlite.JDBC")
        database = Database.connect("jdbc:sqlite:$path?foreign_keys=true", driver = "org.sqlite.JDBC")
        println("Connected to SQLite database, path is : $path")
    }

    fun resetDb(){
        cleanDb()
        initDb()
    }


    fun initDb() {
        println("Database INIT requested.")

        ensureSchemaOrReset()

        // ===== Seed Manager =======
        SeedManager.clear()

        // register seed tasks (order matters if there are FK deps)
        SeedManager.register("admins") { AdminService().seedFromResource("data/admin.json") }
        SeedManager.register("students") { StudentService().seedFromResource("data/students.json") }
        SeedManager.register("teachers") { TeacherService().seedFromResource("data/teachers.json") }



        // run all registered seeders and gather results
        val seedReports = SeedManager.seedAll()

        // Log concise summary
        seedReports.forEach { (name, result) ->
            println("Seed report — $name: inserted=${result.inserted}, skipped=${result.skipped}, errors=${result.errors.size}")
            if (result.errors.isNotEmpty()) {
                result.errors.forEach { err -> println("  • $name error: $err") }
            }
        }

        // Optionally fail fast if a JVM property is set:
        val failOnSeedErrors = System.getProperty("dev.failOnSeedErrors", "false").toBoolean()
        val totalErrors = seedReports.sumOf { it.second.errors.size }
        if (failOnSeedErrors && totalErrors > 0) {
            error("Stopping startup: $totalErrors seed errors (dev.failOnSeedErrors=true)")
        }

        seedModulesIfEmpty()
        seedOptionsIfEmpty()
        seedCoursesIfEmpty()
        seedStudyPlansIfEmpty()
        seedSchedulesIfEmpty()
    }

    private fun ensureSchemaOrReset() {
        try {
            createAllTables()
            validateSchema()
        } catch (e: Exception) {
            if (!looksLikeSchemaMismatch(e)) throw e

            println("Detected incompatible SQLite schema (likely old DB without person_id). Resetting sqlite.db...")
            resetDbFile()

            // Reconnect to the new DB file and recreate schema
            connect()
            createAllTables()
            validateSchema()
        }
    }

    private fun validateSchema() {
        transaction {
            // Force SQLite to validate that all columns expected by Exposed exist.
            PersonTable.selectAll().limit(1).toList()
            AdminTable.selectAll().limit(1).toList()
            TeacherTable.selectAll().limit(1).toList()
            StudentTable.selectAll().limit(1).toList()
            EvaluationTable.selectAll().limit(1).toList()
            ModulesTable.selectAll().limit(1).toList()
            OptionTable.selectAll().limit(1).toList()
            CourseTable.selectAll().limit(1).toList()
            AnnualStudyPlanTable.selectAll().limit(1).toList()
            PlanCourseTable.selectAll().limit(1).toList()
            ScheduleTable.selectAll().limit(1).toList()
        }
    }

    private fun looksLikeSchemaMismatch(error: Throwable): Boolean {
        val markers = listOf(
            "no such column",
            "has no column",
            "has no column named",
            "database schema mismatch",
            "duplicate column name",
            "cannot add a not null column",
            "sql error or missing database",
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
        val path = dbPath ?: "data/sqlite.db"
        val dbFile = File(path)
        if (!dbFile.exists()) return

        // Close registered Exposed database to release file handles.
        database?.let {
            try {
                TransactionManager.closeAndUnregister(it)
            } catch (_: Throwable) {
                // Best-effort.
            }
        }
        database = null

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
            error("Failed to reset sqlite.db (rename & delete failed). Please remove it manually: ${dbFile.absolutePath}")
        }
    }


    /**
     * Fully drop all known tables in reverse-dependency order.
     * This is destructive; use only in dev/CI/testing or behind a protected route.
     */
    fun cleanDb() {
        transaction {
            try {
                SchemaUtils.drop(
                    ScheduleTable,
                    EvaluationTable,
                    PlanCourseTable,
                    AnnualStudyPlanTable,
                    CourseTable,
                    OptionTable,
                    ModulesTable,
                    StudentTable,
                    TeacherTable,
                    AdminTable,
                    PersonTable
                )
                println("✅ All listed tables dropped.")
            } catch (e: Exception) {
                println("⚠️ Error while dropping tables: ${e.message}")
                throw e
            }
        }
    }

    fun createAllTables() {
        transaction {
            try {
                SchemaUtils.create(
                    PersonTable,
                    AdminTable,
                    TeacherTable,
                    StudentTable,
                    EvaluationTable,
                    ModulesTable,
                    OptionTable,
                    CourseTable,
                    AnnualStudyPlanTable,
                    PlanCourseTable,
                    ScheduleTable
                )
                println("✅ Created/ensured all listed tables.")
            } catch (e: Exception) {
                println("⚠️ Error while creating tables: ${e.message}")
                throw e
            }
        }
    }

    private fun seedModulesIfEmpty() {
        val count = transaction { ModulesTable.selectAll().count() }
        if (count == 0L) {
            println("No modules found → skipping automatic seed (method not available)")
            // TODO: Implement module seeding if needed
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

    private fun seedSchedulesIfEmpty() {
        val count = transaction { ScheduleTable.selectAll().count() }
        if (count == 0L) {
            println("No schedules found → seeding from calendar.json")
            val result = ScheduleService().seedFromResource()
            println("Seeding schedules: inserted=${result.inserted} skipped=${result.skipped} errors=${result.errors}")
        } else {
            println("Schedules table already contains $count schedule(s). Skipping seed.")
        }
    }
}
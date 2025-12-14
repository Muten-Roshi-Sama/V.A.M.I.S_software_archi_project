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

// DAO Services
import be.ecam.server.services.AdminService
import be.ecam.server.services.StudentService
import be.ecam.server.services.TeacherService
import be.ecam.server.services.BibleService
import be.ecam.server.services.ScheduleService

// Shared DTO
import be.ecam.common.api.AdminDTO
import be.ecam.common.api.TeacherDTO

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
    fun connect() {
        val dbFolder = File("data")
        if (!dbFolder.exists()) {
            dbFolder.mkdirs()
            println("Created data directory")
        }

        // Get DB file path
        val dbPath = File(dbFolder, "sqlite.db").absolutePath

        //Database.connect("jdbc:sqlite:$dbPath", driver = "org.sqlite.JDBC")
        Database.connect("jdbc:sqlite:$dbPath?foreign_keys=true", driver = "org.sqlite.JDBC")
        println("Connected to SQLite database, path is : $dbPath")
    }

    fun resetDb(){
        cleanDb()
        initDb()
    }


    fun initDb() {
        println("Database INIT requested.")
        
        createAllTables()

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

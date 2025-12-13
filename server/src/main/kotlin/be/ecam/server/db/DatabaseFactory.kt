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
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.selectAll
import java.io.File

object DatabaseFactory {

    fun connect() {
        val dbFolder = File("data")
        if (!dbFolder.exists()) {
            dbFolder.mkdirs()
            println("Created data directory")
        }
        val dbPath = File(dbFolder, "sqlite.db").absolutePath
        Database.connect("jdbc:sqlite:$dbPath", driver = "org.sqlite.JDBC")
        println("Connected to SQLite database, path is : $dbPath")
    }

    fun initDb() {
        createMissingTables()
        initAdmins()
        seedStudentsIfEmpty()
        seedTeachersIfEmpty()
        seedModulesIfEmpty()
        seedOptionsIfEmpty()
        seedCoursesIfEmpty()
        seedStudyPlansIfEmpty()
    }

    private fun createMissingTables() {
        transaction {
            try { SchemaUtils.create(AdminTable) }
            catch (e: Exception) { println("AdminTable already exists.") }

            try { SchemaUtils.create(StudentTable, EvaluationTable) }
            catch (e: Exception) { println("StudentTable & EvaluationTable already exist.") }

            try { SchemaUtils.create(TeacherTable, ModulesTable) }
            catch (e: Exception) { println("TeacherTable & ModulesTable already exist.") }

            try { SchemaUtils.create(OptionTable, CourseTable, AnnualStudyPlanTable, PlanCourseTable) }
            catch (e: Exception) { println("Bible tables already exist.") }
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
        transaction {
            try { SchemaUtils.create(AdminTable); println("AdminTable created.") }
            catch (e: Exception) { println("AdminTable already exists.") }
        }

        val service = AdminService()
        val existing = service.getAll()

        if (existing.isEmpty()) {
            val adminDTOs = Json.decodeFromString<List<AdminDTO>>(jsonString)
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
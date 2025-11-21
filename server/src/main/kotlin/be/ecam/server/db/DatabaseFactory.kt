package be.ecam.server.db

// DAO Schema
import be.ecam.server.models.AdminTable
import be.ecam.server.models.EvaluationTable
import be.ecam.server.models.StudentTable   //Add
//import be.ecam.server.models.CourseTable
//import be.ecam.server.models.TeacherTable
//import be.ecam.server.db.StudentTable

// DAO Class
import be.ecam.server.models.Admin
//import be.ecam.server.models.Course
//import be.ecam.server.models.Teacher
//import be.ecam.server.models.Student

// Shared DTO
import be.ecam.common.api.AdminDTO
//import be.ecam.common.aoi.CourseDTO
//import be.ecam.common.aoi.TeacherDTO
//import be.ecam.common.aoi.DTO
//import be.ecam.common.aoi.CourseDTO

// DAO Services
import be.ecam.server.services.AdminService
import be.ecam.server.services.StudentService



import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.selectAll           //Add
import java.io.File

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


        Database.connect("jdbc:sqlite:$dbPath", driver = "org.sqlite.JDBC")
        println("Connected to SQLite database, path is : $dbPath")

//        transaction {
////            SchemaUtils.createMissingTablesAndColumns(AdminTable)
//            SchemaUtils.create(AdminTable)
//
//        }

    }

    fun initDb() {
        createMissingTables()
        initAdmins()
        seedStudentsIfEmpty()
//        initTeachers()

    }

    private fun createMissingTables() {
        transaction {
            try { SchemaUtils.create(AdminTable) }
            catch (e: Exception) { println("AdminTable already exists.") }

            try { SchemaUtils.create(StudentTable, EvaluationTable) }
            catch (e: Exception) { println("StudentTable & EvaluationTable already exist.") }
        }
    }

    private fun seedStudentsIfEmpty() {
        val count = transaction {
            StudentTable.selectAll().count()
        }
// ===========================================================================

        if (count == 0L) {
            println("No students found → seeding from students.json")
            StudentService().seedFromJson()
        } else {
            println("Student table already contains $count student(s). Skipping seed.")
        }
    }

    // Ton code initAdmins() (inchangé)
    private fun initAdmins() {
        // Try multiple possible paths for the JSON file
        val possiblePaths = listOf(
            "server/src/main/resources/data/admin.json",
            "src/main/resources/data/admin.json",
            "data/admin.json"
        )

        val adminFile = possiblePaths
            .map { File(it) }
            .firstOrNull { it.exists() }

        if (adminFile == null) {
            // Try loading from classpath as fallback
            val resourceStream = this::class.java.classLoader.getResourceAsStream("data/admin.json")
            if (resourceStream != null) {
                val jsonString = resourceStream.bufferedReader().use { it.readText() }
                processAdminData(jsonString)
                return
            }
            println("⚠️ No mock data file found. Tried paths:")
            possiblePaths.forEach { println("   - $it") }
            println("   - classpath: data/admin.json")
            return
        }

        println("Loading admin data from: ${adminFile.absolutePath}")
        val jsonString = adminFile.readText()
        processAdminData(jsonString)
    }

    private fun processAdminData(jsonString: String) {
        transaction {
            // Use create() instead of createMissingTablesAndColumns()
            // This will only work on first run; on subsequent runs it will throw an exception we'll catch
            try {
                SchemaUtils.create(AdminTable)
                println("✅ AdminTable created.")
            } catch (e: Exception) {
                // Table already exists, which is fine
                println("ℹ️ AdminTable already exists.")
            }
        }

        // Use service for business logic
        val service = AdminService()
        val existing = service.getAll()

        if (existing.isEmpty()) {
            val adminDTOs = Json.decodeFromString<List<AdminDTO>>(jsonString)
            adminDTOs.forEach { service.create(it) }
            println("Inserted ${adminDTOs.size} mock admins from JSON.")

            adminDTOs.forEach { dto ->
                service.create(dto)  // ← Service handles DTO → DAO conversion
            }

            println("✅ Inserted ${adminDTOs.size} mock admins from JSON.")

            // ✅ DEBUG: Print all admins in DB
            transaction {
                println("=== Current Admins in DB ===")
                Admin.all().forEach { println("ID=${it.id.value} | username=${it.username} | email=${it.email}") }
                for (admin in Admin.all()) {
                    println("ID=${admin.id.value} | username=${admin.username} | email=${admin.email}")
                }
                println("============================")
            }
        } else {
            println("Admin table already contains ${existing.size} admins.")
            println("ℹ️ Admin table already contains ${existing.size} admins.")
        }
    }
}

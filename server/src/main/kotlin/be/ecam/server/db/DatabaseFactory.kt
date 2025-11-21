package be.ecam.server.db

import org.jetbrains.exposed.sql.Database    // Imports Exposed’s Database object which manages the JDBC connection for Exposed (the JetBrains SQL library).

// DAO Schema
import be.ecam.server.models.AdminTable
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



import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
// ====================================================================
object DatabaseFactory {
    fun connect() {

        val dbFolder = File("data")
        if (!dbFolder.exists()){
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
        initAdmins()
//        initTeachers()

    }


// ===========================================================================


    private fun initAdmins() {
        // Try multiple possible paths for the JSON file
        val possiblePaths = listOf(
            "server/src/main/resources/data/admin.json",  // When running from project root
            "src/main/resources/data/admin.json",         // When running from server directory
            "data/admin.json"                              // Alternative location
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

        println("📂 Loading admin data from: ${adminFile.absolutePath}")
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

            adminDTOs.forEach { dto ->
                service.create(dto)  // ← Service handles DTO → DAO conversion
            }

            println("✅ Inserted ${adminDTOs.size} mock admins from JSON.")

            // ✅ DEBUG: Print all admins in DB
            transaction {
                println("=== Current Admins in DB ===")
                for (admin in Admin.all()) {
                    println("ID=${admin.id.value} | username=${admin.username} | email=${admin.email}")
                }
                println("============================")
            }
        } else {
            println("ℹ️ Admin table already contains ${existing.size} admins.")
        }
    }
}

package be.ecam.server.db

// DAO Schema
import be.ecam.server.models.PersonTable
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

// DAO Services
import be.ecam.server.services.AdminService
import be.ecam.server.services.StudentService

// Shared DTO
import be.ecam.common.api.AdminDTO
//import be.ecam.common.api.CourseDTO
//import be.ecam.common.api.TeacherDTO
//import be.ecam.common.api.DTO
//import be.ecam.common.api.CourseDTO


// Kotlin imports
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager

import org.jetbrains.exposed.sql.selectAll           //Add
import java.io.File
import java.sql.Statement
import org.jetbrains.exposed.sql.selectAll

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

//        Database.connect("jdbc:sqlite:$dbPath", driver = "org.sqlite.JDBC")
        Database.connect("jdbc:sqlite:$dbPath?foreign_keys=true", driver = "org.sqlite.JDBC")
        println("Connected to SQLite database, path is : $dbPath")
    }

    /**
         * Enable SQLite PRAGMA foreign_keys = ON using a plain JDBC Statement inside an Exposed transaction.
         * Using TransactionManager.current().connection and a JDBC execute avoids Exposed's "Query does not return results"
         * exception when executing PRAGMA statements that don't return a result set.
     */
//        fun enableForeignKeys() {
//                try {
//                        transaction {
//                                val conn = TransactionManager.current().connection
//                                try {
//                                        conn.createStatement().use { stmt: Statement ->
//                                                stmt.execute("PRAGMA foreign_keys = ON;")
//                                            }
//                                        println("✅ SQLite PRAGMA foreign_keys = ON applied.")
//                                    } catch (e: Exception) {
//                                        println("⚠️ Failed to apply PRAGMA foreign_keys: ${e.message}")
//                                    }
//                            }
//                    } catch (e: Exception) {
//                        // If transactions are not available for some reason, still log the problem.
//                        println("⚠️ enableForeignKeys() failed: ${e.message}")
//                    }
//            }
//


    fun resetDb(){
        cleanDb()
        initDb()
    }


    fun initDb() {
        println("Database reset requested.")
//        createMissingTables()
//        initAdmins()
//        seedStudentsIfEmpty()
//        initTeachers()
        createAllTables()


        // ===== Seed Manager =======
        SeedManager.clear()

        // register seed tasks (order matters if there are FK deps)
        SeedManager.register("admins") { AdminService().seedFromResource("data/admin.json") }
        // SeedManager.register("students") { StudentService().seedFromResource("data/students.json") }



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

    }


    /**
     * Fully drop all known tables in reverse-dependency order.
     * This is destructive; use only in dev/CI/testing or behind a protected route.
     */
    fun cleanDb() {
        transaction {
            try {
                // Drop the tables you want to reset. Adjust the list to match your project's Table objects.
                SchemaUtils.drop(
                    // drop dependents first
//                    GradesTable,
//                    EnrollmentTable,
//                    OfferingTable,
//                    EvaluationTable,
                    // role / entity tables
//                    StudentTable,
//                    TeacherTable,
                    AdminTable,
//                    CourseTable,
                    PersonTable
                )
                println("✅ All listed tables dropped.")
            } catch (e: Exception) {
                // Log and rethrow so caller routes can respond with error
                println("⚠️ Error while dropping tables: ${e.message}")
                throw e
            }
        }
    }

    /**
     * Create all tables in proper order (parents first).
     * This will create the listed tables if they do not exist.
     */
    fun createAllTables() {
        transaction {
            try {
                SchemaUtils.create(
                    PersonTable,
//                    CourseTable,
                    // role tables
                    AdminTable,
//                    TeacherTable,
//                    StudentTable,
//                    // domain/dependent tables
//                    EvaluationTable,
//                    OfferingTable,
//                    EnrollmentTable,
//                    GradesTable
                )
                println("✅ Created/ensured all listed tables.")
            } catch (e: Exception) {
                println("⚠️ Error while creating tables: ${e.message}")
                throw e
            }
        }
    }










//    private fun createMissingTables() {
//        transaction {
//            try {
////                SchemaUtils.create(AdminTable)
//                SchemaUtils.createMissingTablesAndColumns(
////                    PersonTable,
//                    AdminTable,
////                    TeacherTable,
////                    StudentTable,
////                    CourseTable,        // if you have it
////                    EvaluationTable,
////                    OfferingTable,     // if present
////                    EnrollmentTable,
////                    GradesTable
//                )
//
//
//
//            } catch (e: Exception) { println("error while createMissingTables : ${e.message}") }
//
//            try { SchemaUtils.create(StudentTable, EvaluationTable) }
//            catch (e: Exception) { println("StudentTable & EvaluationTable already exist.") }
//        }
//    }

//    private fun seedStudentsIfEmpty() {
//        val count = transaction {
//            StudentTable.selectAll().count()
//        }
//// ===========================================================================
//
//        if (count == 0L) {
//            println("No students found → seeding from students.json")
//            StudentService().seedFromJson()
//        } else {
//            println("Student table already contains $count student(s). Skipping seed.")
//        }
//    }

    // Ton code initAdmins() (inchangé)
//    private fun initAdmins() {
//        // Try multiple possible paths for the JSON file
//        val possiblePaths = listOf(
//            "server/src/main/resources/data/admin.json",
//            "src/main/resources/data/admin.json",
//            "data/admin.json"
//        )
//
//        val adminFile = possiblePaths
//            .map { File(it) }
//            .firstOrNull { it.exists() }
//
//        if (adminFile == null) {
//            // Try loading from classpath as fallback
//            val resourceStream = this::class.java.classLoader.getResourceAsStream("data/admin.json")
//            if (resourceStream != null) {
//                val jsonString = resourceStream.bufferedReader().use { it.readText() }
//                processAdminData(jsonString)
//                return
//            }
//            println("⚠️ No mock data file found. Tried paths:")
//            possiblePaths.forEach { println("   - $it") }
//            println("   - classpath: data/admin.json")
//            return
//        }
//
//        println("Loading admin data from: ${adminFile.absolutePath}")
//        val jsonString = adminFile.readText()
//        processAdminData(jsonString)
//    }

//    private fun processAdminData(jsonString: String) {
//        transaction {
//            // Use create() instead of createMissingTablesAndColumns()
//            // This will only work on first run; on subsequent runs it will throw an exception we'll catch
//            try {
//                SchemaUtils.create(AdminTable)
//                println("✅ AdminTable created.")
//            } catch (e: Exception) {
//                // Table already exists, which is fine
//                println("ℹ️ AdminTable already exists.")
//            }
//        }
//
//        // Use service for business logic
//        val service = AdminService()
//        val existing = service.getAll()
//
//        if (existing.isEmpty()) {
//            val adminDTOs = Json.decodeFromString<List<AdminDTO>>(jsonString)
//            adminDTOs.forEach { service.create(it) }
//            println("Inserted ${adminDTOs.size} mock admins from JSON.")
//
//            adminDTOs.forEach { dto ->
//                service.create(dto)  // ← Service handles DTO → DAO conversion
//            }
//
//            println("✅ Inserted ${adminDTOs.size} mock admins from JSON.")
//
//            // ✅ DEBUG: Print all admins in DB
//            transaction {
//                println("=== Current Admins in DB ===")
//                Admin.all().forEach { println("ID=${it.id.value} | username=${it.username} | email=${it.email}") }
//                for (admin in Admin.all()) {
//                    println("ID=${admin.id.value} | username=${admin.username} | email=${admin.email}")
//                }
//                println("============================")
//            }
//        } else {
//            println("Admin table already contains ${existing.size} admins.")
//            println("ℹ️ Admin table already contains ${existing.size} admins.")
//        }
//    }




}

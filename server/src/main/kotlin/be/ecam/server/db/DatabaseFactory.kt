package be.ecam.server.db

// DAO Schema
import be.ecam.server.models.PersonTable
import be.ecam.server.models.AdminTable
// import be.ecam.server.models.EvaluationTable
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

                    // GradesTable,
                    // EnrollmentTable,
                    // OfferingTable,
                    // EvaluationTable,

                    // role / entity tables
                    StudentTable,
                    // TeacherTable,
                    AdminTable,
                    // CourseTable,
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
                    // CourseTable,

                    // role tables
                    AdminTable,
                    // TeacherTable,
                    StudentTable,

                    // // domain/dependent tables
                    // EvaluationTable,
                    // OfferingTable,
                    // EnrollmentTable,
                    // GradesTable
            )
                println("✅ Created/ensured all listed tables.")
            } catch (e: Exception) {
                println("⚠️ Error while creating tables: ${e.message}")
                throw e
            }
        }
    }




}




// END